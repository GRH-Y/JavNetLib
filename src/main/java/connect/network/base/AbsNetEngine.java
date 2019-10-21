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
        //必须重写，不然该方法没有权限访问
        onEngineRun();
    }

    abstract protected void onEngineRun();

    protected boolean isEngineRunning() {
        return mExecutor != null ? mExecutor.getLoopState() : false;
    }

    protected void resumeEngine() {
        if (mExecutor != null) {
            mExecutor.resumeTask();
        }
    }

    protected void startEngine() {
        if (mContainer == null) {
            mContainer = TaskExecutorPoolManager.getInstance().createLoopTask(this, null);
            mExecutor = mContainer.getTaskExecutor();
            mExecutor.blockStartTask();
        }
    }

    protected void stopEngine() {
        if (mContainer != null) {
            mExecutor.blockStopTask();
            mContainer = null;
        }
    }

}