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

    private final static String TAG = BaseNetEngine.class.getName();

    /**
     * 创建任务
     */
    public static final byte CREATE = 1;

    /**
     * 销毁任务
     */
    public static final byte DESTROY = 4;

    protected ILoopTaskExecutor mExecutor = null;

    protected FactoryContext mFactoryContext;

    protected byte mWorkStep = (byte) (CREATE | DESTROY);

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

    public BaseNetEngine(FactoryContext context) {
        if (context == null) {
            throw new NullPointerException("FactoryContext can not be null !!!");
        }
        this.mFactoryContext = context;
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
        BaseNetWork netWork = mFactoryContext.getNetWork();
        netWork.onWorkBegin();
//        SpiderEnvoy.getInstance().pinKeyProbe(TAG);
    }


    /**
     * engine run method of looping
     */
    protected void onEngineRun() {
        BaseNetWork netWork = mFactoryContext.getNetWork();
        if ((mWorkStep & CREATE) == CREATE) {
            // 检测是否有新的任务添加
            netWork.onConnectNetTaskBegin();
        }
//        SpiderEnvoy.getInstance().pinKeyProbe(TAG);
        onCreateStepExt();
//        SpiderEnvoy.getInstance().pinKeyProbe(TAG);
        if ((mWorkStep & DESTROY) == DESTROY) {
            // 清除要结束的任务
            netWork.onDisconnectNetTaskEnd();
        }
//        SpiderEnvoy.getInstance().pinKeyProbe(TAG);
        onDestroyStepExt();
//        SpiderEnvoy.getInstance().pinKeyProbe(TAG);
    }

    protected void onCreateStepExt() {

    }

    protected void onDestroyStepExt() {

    }


    protected void onEngineDestroy() {
        BaseNetWork netWork = mFactoryContext.getNetWork();
        netWork.onWorkEnd();
        release();
//        SpiderEnvoy.getInstance().pinKeyProbe(TAG);
    }

    //----------------------------------------------------------------------------------


    public void setWorkStep(byte process) {
        this.mWorkStep = process;
    }


    public void resumeEngine() {
        if (mExecutor != null) {
            mExecutor.resumeTask();
//            SpiderEnvoy.getInstance().pinKeyProbe(TAG);
        }
    }

    public void pauseEngine() {
        if (mExecutor != null) {
            mExecutor.pauseTask();
//            SpiderEnvoy.getInstance().pinKeyProbe(TAG);
        }
    }

    public void startEngine() {
        if (mExecutor == null || !mExecutor.isAliveState() || mExecutor.isStopState()) {
            BaseNetWork baseNetWork = mFactoryContext.getNetWork();
            TaskContainer container = new TaskContainer(mEngineCore, baseNetWork.getClass().getName());
            mExecutor = container.getTaskExecutor();
            mExecutor.startTask();
//            SpiderEnvoy.getInstance().startWatchKey(TAG);
        }
    }

    public void stopEngine() {
        if (mExecutor != null) {
            mExecutor.stopTask();
//            SpiderEnvoy.getInstance().endWatchKey(TAG);
        }
    }

    public void release() {
        mExecutor = null;
        mFactoryContext = null;
    }

}
