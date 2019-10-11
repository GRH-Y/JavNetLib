package connect.network.nio;


import task.executor.BaseLoopTask;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ITaskContainer;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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


    private final class CoreSendTask extends BaseLoopTask {

        private ITaskContainer taskContainer = null;
        private Queue<byte[]> mCache = new ConcurrentLinkedQueue();


        void addData(byte[] data) {
            mCache.add(data);
            synchronized (CoreSendTask.class) {
                if (taskContainer == null) {
                    taskContainer = TaskExecutorPoolManager.getInstance().runTask(this, null);
                }
            }
            synchronized (CoreSendTask.class) {
                if (taskContainer == null) {
                    taskContainer = TaskExecutorPoolManager.getInstance().runTask(this, null);
                }
            }
        }

        @Override
        protected void onRunLoopTask() {
            while (!mCache.isEmpty()) {
                byte[] data = null;
                try {
                    data = mCache.poll();
                } catch (Exception e) {
                }
                if (data != null) {
                    boolean isError = true;
                    try {
                        if (channel.isOpen() && channel.isConnected() && !clientTask.isCloseing()) {
                            isError = channel.write(ByteBuffer.wrap(data)) < 0;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    if (isError) {
                        onSenderErrorCallBack();
                        break;
                    }
                }
            }
            synchronized (CoreSendTask.class) {
                taskContainer.getTaskExecutor().stopTask();
                taskContainer = null;
            }
        }

    }
}
