package connect.network.nio;


import log.LogDog;
import task.executor.BaseConsumerTask;
import task.executor.ConsumerQueueAttribute;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.IConsumerTaskExecutor;
import task.executor.joggle.ITaskContainer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 高性能的发送者 (独立线程处理发送数据)
 */
public class NioHPCSender extends NioSender {

    private CoreSendTask coreSendTask;

    public NioHPCSender() {
        coreSendTask = new CoreSendTask();
    }

    /**
     * 发送数据
     *
     * @param data
     */
    @Override
    public void sendData(byte[] data) {
        coreSendTask.addData(data);
    }


    @Override
    protected void setChannel(SocketChannel channel) {
        super.setChannel(channel);
        if (channel != null) {
            coreSendTask.start();
        }
    }

    /**
     * 销毁线程，任务结束一定要调用，不然会内存泄露
     */
    public void destroy() {
        coreSendTask.stop();
    }

    private final class CoreSendTask extends BaseConsumerTask<byte[]> {

        private ITaskContainer taskContainer;
        private IConsumerAttribute<byte[]> attribute;

        CoreSendTask() {
            taskContainer = TaskExecutorPoolManager.getInstance().createJThread(this);
            attribute = new ConsumerQueueAttribute<>();
            taskContainer.setAttribute(attribute);
            IConsumerTaskExecutor executor = taskContainer.getTaskExecutor();
            executor.setIdleStateSleep(true);
        }

        void addData(byte[] data) {
            attribute.pushToCache(data);
            taskContainer.getTaskExecutor().resumeTask();
        }

        void start() {
            taskContainer.getTaskExecutor().startTask();
        }

        void stop() {
            taskContainer.getTaskExecutor().stopTask();
        }

        @Override
        protected void onProcess() {
            byte[] data = attribute.popCacheData();
            if (data != null) {
                boolean isError = true;
                try {
                    if (channel.isOpen() && channel.isConnected()) {
                        isError = channel.write(ByteBuffer.wrap(data)) <= 0;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                if (isError) {
                    onSenderErrorCallBack();
                    LogDog.e("NioSender onWrite error  !!!");
                }

            }
        }
    }

    /**
     * 发送数据失败回调
     */
    protected void onSenderErrorCallBack() {
    }
}
