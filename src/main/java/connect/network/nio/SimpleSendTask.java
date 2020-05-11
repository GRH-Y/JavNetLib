package connect.network.nio;


import task.executor.BaseConsumerTask;
import task.executor.ConsumerQueueAttribute;
import task.executor.TaskContainer;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.IConsumerTaskExecutor;
import task.executor.joggle.ITaskContainer;
import util.MultiplexCache;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleSendTask {

    private final class SendEntity {

        private NioSender sender;
        private ByteBuffer data;

        SendEntity(NioSender sender, ByteBuffer data) {
            this.sender = sender;
            this.data = data;
        }

        public void set(NioSender sender, ByteBuffer data) {
            this.sender = sender;
            this.data = data;
        }

        public NioSender getSender() {
            return sender;
        }

        public ByteBuffer getData() {
            return data;
        }

    }

    private volatile static SimpleSendTask simpleSendTask;

    private IConsumerAttribute<SendEntity> attribute;
    private ITaskContainer container;
    private MultiplexCache<SendEntity> sendEntityCache;

    public static SimpleSendTask getInstance() {
        if (simpleSendTask == null) {
            synchronized (SimpleSendTask.class) {
                if (simpleSendTask == null) {
                    simpleSendTask = new SimpleSendTask();
                }
            }
        }
        return simpleSendTask;
    }

    private SimpleSendTask() {
        attribute = new ConsumerQueueAttribute();
        container = new TaskContainer(new CoreSendTask());
        container.getTaskExecutor().setAttribute(attribute);
        IConsumerTaskExecutor iConsumerTaskExecutor = container.getTaskExecutor();
        iConsumerTaskExecutor.setIdleStateSleep(true);
        sendEntityCache = new MultiplexCache();
    }

    public void sendData(NioSender sender, byte[] data) {
        if (sender != null && data != null && data.length > 0) {
            sendData(sender, ByteBuffer.wrap(data));
        }
    }

    public void sendData(NioSender sender, ByteBuffer data) {
        if (sender != null && data != null && data.hasRemaining()) {
            SendEntity sendEntity = sendEntityCache.getCanUseData();
            if (sendEntity == null) {
                sendEntity = new SendEntity(sender, data);
                sendEntityCache.addData(sendEntity);
            } else {
                sendEntity.set(sender, data);
            }
            attribute.pushToCache(sendEntity);
            container.getTaskExecutor().resumeTask();
        }
    }

    public void open() {
        container.getTaskExecutor().startTask();
    }

    public void close() {
        container.getTaskExecutor().stopTask();
    }


    class CoreSendTask extends BaseConsumerTask {

        @Override
        protected void onProcess() {
            SendEntity sendEntity = attribute.popCacheData();
            if (sendEntity != null) {
                NioSender sender = sendEntity.getSender();
                ByteBuffer buffer = sendEntity.getData();
                Exception exception = null;
                int ret;
                try {
                    do {
                        ret = sender.channel.write(buffer);
                        if (ret < 0) {
                            throw new IOException("SocketChannel is bad !!!");
                        } else if (ret == 0 && buffer.hasRemaining() && sender.channel.isOpen()) {
                            //当前管道负载过高没有发送完成，等待下次触发
                            attribute.pushToCache(sendEntity);
                        }
                    }
                    while (ret > 0 && buffer.hasRemaining() && sender.channel.isOpen());
                } catch (IOException e) {
                    exception = e;
                    e.printStackTrace();
                } finally {
                    if (sender.feedback != null) {
                        sender.feedback.onSenderFeedBack(sender, buffer, exception);
                    }
                }
            }
        }

        @Override
        protected void onDestroyTask() {
            sendEntityCache.release();
        }
    }
}
