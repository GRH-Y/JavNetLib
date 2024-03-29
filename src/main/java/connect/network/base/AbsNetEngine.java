package connect.network.base;

import task.executor.BaseLoopTask;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;

public abstract class AbsNetEngine extends BaseLoopTask {

    protected ILoopTaskExecutor mExecutor = null;
    protected ITaskContainer mContainer = null;

    @Override
    protected void onRunLoopTask() {
        onEngineRun();
    }

    abstract protected void onEngineRun();

    protected boolean isEngineRunning() {
        return mExecutor != null ? mExecutor.isLoopState() && mExecutor.isStartState() : false;
    }

    protected void resumeEngine() {
        if (mExecutor != null) {
            mExecutor.resumeTask();
        }
    }

    protected void startEngine() {
        if (mContainer == null) {
            // createLoopTask use time 20 ms
            mContainer = TaskExecutorPoolManager.getInstance().createLoopTask(this, null);
            mExecutor = mContainer.getTaskExecutor();
            mExecutor.startTask();
        }
    }

    protected void stopEngine() {
        if (mContainer != null) {
            mExecutor.destroyTask();
            mExecutor = null;
            mContainer = null;
        }
    }

}
