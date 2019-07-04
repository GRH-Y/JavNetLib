package connect.network.base;

import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;

public class LowPcEngine extends PcEngine {

    protected ILoopTaskExecutor mExecutor = null;
    protected ITaskContainer mContainer = null;

    @Override
    protected void startEngine() {
        if (mContainer == null) {
            mContainer = TaskExecutorPoolManager.getInstance().createJThread(this);
            mExecutor = mContainer.getTaskExecutor();
            mExecutor.startTask();
        } else {
            TaskExecutorPoolManager.getInstance().runTask(this, null);
        }
    }

    @Override
    protected void stopEngine() {
        if (mExecutor != null) {
            mExecutor.blockStopTask();
        }
    }
}
