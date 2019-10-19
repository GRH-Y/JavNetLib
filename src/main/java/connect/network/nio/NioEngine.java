package connect.network.nio;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;

public class NioEngine extends AbsNetEngine {

    protected AbsNetFactory mFactory;

    protected NioNetWork mWork;

    public NioEngine(AbsNetFactory factory, NioNetWork work) {
        this.mFactory = factory;
        this.mWork = work;
    }

    @Override
    protected void onEngineRun() {
        //检测是否有新的任务添加
        mWork.onCheckConnectTask(false);
        //检查是否有读写任务
        mWork.onExecuteTask();
        //清除要结束的任务
        mWork.onCheckRemoverTask(false);
    }

    @Override
    protected void resumeEngine() {
        resumeEngine(true);
    }

    protected void resumeEngine(boolean isNeedWakeup) {
        if (isNeedWakeup && mWork.getSelector() != null) {
            mWork.getSelector().wakeup();
        }
    }

    @Override
    protected void onDestroyTask() {
        mWork.onRecoveryTaskAll();
    }

}
