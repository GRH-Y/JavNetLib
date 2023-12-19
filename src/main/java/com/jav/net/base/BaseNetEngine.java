package com.jav.net.base;


import com.jav.thread.executor.LoopTask;
import com.jav.thread.executor.TaskContainer;
import com.jav.thread.executor.joggle.ILoopTaskExecutor;

/**
 * base net engine
 *
 * @author yyz
 */
public class BaseNetEngine {


    protected ILoopTaskExecutor mExecutor = null;


    protected BaseNetWork mNetWork;
    protected EngineCore mEngineCore;

    protected class EngineCore extends LoopTask {
        @Override
        protected void onInitTask() {
            onEngineInit();
        }

        @Override
        protected void onRunLoopTask() {
            onEngineRun();
        }

        @Override

        protected void onDestroyTask() {
            onEngineDestroy();
        }

    }

    public BaseNetEngine(BaseNetWork work) {
        this.mNetWork = work;
        mEngineCore = new EngineCore();
    }


    protected boolean isEngineRunning() {
        return mExecutor != null && mExecutor.isLoopState() && mExecutor.isAliveState();
    }

    protected boolean isEngineStop() {
        return mExecutor != null && mExecutor.isStopState();
    }


    //----------------------------------------------------------------------------------

    protected void onEngineInit() {
//        LogDog.d("#engine# run onWorkBegin start " + mNetWork);
        mNetWork.onWorkBegin();
//        LogDog.d("#engine# run onWorkBegin end " + mNetWork);
    }


    /**
     * engine run method of looping
     */
    protected void onEngineRun() {
//        LogDog.d("#engine# run onWorkRun start " + mNetWork);
        mNetWork.onWorkRun();
//        LogDog.d("#engine# run onWorkRun end " + mNetWork);
    }


    protected void onEngineDestroy() {
        mNetWork.onWorkEnd();
    }

    //----------------------------------------------------------------------------------


    public void resumeEngine() {
        if (mExecutor != null) {
            mExecutor.resumeTask();
        }
    }

    public void pauseEngine() {
        if (mExecutor != null) {
            mExecutor.pauseTask();
        }
    }

    public void startEngine() {
        if (mExecutor == null) {
            TaskContainer container = new TaskContainer(mEngineCore, getEngineName());
            mExecutor = container.getTaskExecutor();
            mExecutor.startTask();
        }
    }

    public void stopEngine() {
        if (mExecutor != null) {
            mExecutor.stopTask();
            mExecutor = null;
        }
    }

    private String getEngineName() {
        String[] workName = mNetWork.toString().split("\\.");
        String[] engineName = toString().split("\\.");
        return workName[workName.length - 1] + ":" + engineName[engineName.length - 1];
    }


}
