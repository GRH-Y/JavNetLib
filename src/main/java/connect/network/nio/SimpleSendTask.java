package connect.network.nio;


import task.executor.BaseConsumerTask;
import task.executor.ConsumerQueueAttribute;
import task.executor.TaskContainer;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.IConsumerTaskExecutor;
import task.executor.joggle.ITaskContainer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SimpleSendTask {

    private final class SendEntity {

        private NioSender nioSender;
        private byte[] data;

        SendEntity(NioSender nioSender, byte[] data) {
            this.nioSender = nioSender;
            this.data = data;
        }

        public NioSender getNioSender() {
            return nioSender;
        }

        public byte[] getData() {
            return data;
        }
    }

    private static SimpleSendTask simpleSendTask;

    private IConsumerAttribute<SendEntity> attribute;
    private ITaskContainer container;

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
    }

    public void sendData(NioSender nioSender, byte[] data) {
        if (nioSender != null && data != null) {
            attribute.pushToCache(new SendEntity(nioSender, data));
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
            SocketChannel channel = sendEntity.getNioSender().getChannel();
            NioClientTask clientTask = sendEntity.getNioSender().clientTask;
            if (channel == null) {
                if (clientTask != null && !clientTask.isTaskNeedClose()) {
                    //该任务还没准备就绪，延迟发送数据
                    attribute.pushToCache(sendEntity);
                }
                return;
            }
            boolean isError = true;
            try {
                if (channel.isOpen() && channel.isConnected() && !clientTask.isTaskNeedClose()) {
                    int len = channel.write(ByteBuffer.wrap(sendEntity.getData()));
                    isError = len < 0;
                    if (len == 0) {
                        attribute.pushToCache(sendEntity);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (isError) {
                sendEntity.getNioSender().onSenderErrorCallBack();
            }
        }

    }
}
