package com.jav.net.base;


import com.jav.net.entity.FactoryContext;
import com.jav.thread.executor.LoopTask;
import com.jav.thread.executor.TaskContainer;
import com.jav.thread.executor.joggle.ILoopTaskExecutor;

/**
 * base net engine
 *
 * @author yyz
 */
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

    /**
     * engine run method of looping
     */
    abstract protected void onEngineRun();

    protected boolean isEngineRunning() {
        return mExecutor != null && mExecutor.isLoopState() && mExecutor.isAliveState();
    }

    protected boolean isEngineStoping() {
        return mExecutor != null && mExecutor.isStopState();
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
        if (mExecutor == null || !mExecutor.isAliveState()) {
            TaskContainer container = new TaskContainer(this);
            mExecutor = container.getTaskExecutor();
            mExecutor.startTask();
        }
    }

    protected void stopEngine() {
        if (mExecutor != null) {
            mExecutor.stopTask();
        }
    }

    protected void release() {
        mExecutor = null;
        mFactoryContext = null;
    }

}
