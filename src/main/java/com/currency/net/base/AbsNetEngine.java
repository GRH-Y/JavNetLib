package com.currency.net.base;


import com.currency.net.entity.FactoryContext;
import task.executor.LoopTask;
import task.executor.TaskContainer;
import task.executor.joggle.ILoopTaskExecutor;

public abstract class AbsNetEngine extends LoopTask {

    protected ILoopTaskExecutor mExecutor = null;
    protected FactoryContext mFactoryContext;

    public AbsNetEngine(FactoryContext context) {
        if (context == null) {
            throw new NullPointerException("FactoryContext can not be null !!!");
        }
        this.mFactoryContext = context;
    }

    @Override
    protected void onRunLoopTask() {
        onEngineRun();
    }

    abstract protected void onEngineRun();

    protected boolean isEngineRunning() {
        return mExecutor != null && mExecutor.isLoopState() && mExecutor.isStartState();
    }

    protected void resumeEngine() {
        if (mExecutor != null) {
            mExecutor.resumeTask();
        }
    }

    protected void pauseEngine() {
        if (mExecutor != null) {
            mExecutor.pauseTask();
        }
    }

    protected void startEngine() {
        if (mExecutor == null) {
            TaskContainer container = new TaskContainer(this);
            mExecutor = container.getTaskExecutor();
            mExecutor.startTask();
        }
    }

    protected void stopEngine() {
        if (mExecutor != null) {
            mExecutor.destroyTask();
            mExecutor = null;
        }
    }

}
