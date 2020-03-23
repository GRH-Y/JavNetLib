package connect.network.nio;


import task.executor.BaseConsumerTask;
import task.executor.ConsumerQueueAttribute;
import task.executor.TaskContainer;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.IConsumerTaskExecutor;
import task.executor.joggle.ITaskContainer;

import java.io.IOException;

public class SimpleSendTask {

    private final class SendEntity {

        private NioSender sender;
        private byte[] data;

        SendEntity(NioSender sender, byte[] data) {
            this.sender = sender;
            this.data = data;
        }

        public NioSender getSender() {
            return sender;
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

    public void sendData(NioSender sender, byte[] data) {
        if (sender != null && data != null) {
            attribute.pushToCache(new SendEntity(sender, data));
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
            NioSender sender = sendEntity.getSender();
            try {
                sender.sendDataImp(sendEntity.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
