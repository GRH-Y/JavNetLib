package connect.network.nio;

import connect.network.base.AbsNetEngine;

public class NioEngine extends AbsNetEngine {

    protected NioNetWork mWork;

    public NioEngine(NioNetWork work) {
        this.mWork = work;
    }

    @Override
    protected void onInitTask() {
        mWork.init();
    }

    @Override
    protected void onEngineRun() {
        //检测是否有新的任务添加
        mWork.onCheckConnectTask();
        //检查是否有读写任务
        mWork.onExecuteTask();
        //清除要结束的任务
        mWork.onCheckRemoverTask();
    }

    @Override
    protected void resumeEngine() {
        if (mWork.getSelector() != null) {
            mWork.getSelector().wakeup();
        }
    }

    @Override
    protected void onDestroyTask() {
        mWork.onRecoveryTaskAll();
    }

    @Override
    protected void stopEngine() {
        resumeEngine();
        super.stopEngine();
    }
}
