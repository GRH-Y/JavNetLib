package connect.network.base;

import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;

public class LowPcEngine extends PcEngine {

    protected ILoopTaskExecutor mExecutor = null;
    protected ITaskContainer mContainer = null;

    @Override
    protected boolean isRunning() {
        return mExecutor != null ? mExecutor.getLoopState() : false;
    }

    @Override
    protected void resumeTask() {
        if (mExecutor != null) {
            mExecutor.resumeTask();
        }
    }

    @Override
    protected void startEngine() {
        if (mContainer == null) {
            mContainer = TaskExecutorPoolManager.getInstance().createJThread(this);
            mExecutor = mContainer.getTaskExecutor();
            mExecutor.blockStartTask();
        } else {
            TaskExecutorPoolManager.getInstance().runTask(this, null);
        }
    }

    @Override
    protected void stopEngine() {
        if (mExecutor != null) {
//            mExecutor.blockStopTask();
            TaskExecutorPoolManager.getInstance().destroy(mContainer);
            mContainer = null;
        }
    }
}
