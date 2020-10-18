package connect.network.aio;

import connect.network.base.AbsNetEngine;

public class AioEngine extends AbsNetEngine {

    protected AioNetWork mWork;

    public AioEngine(AioNetWork work) {
        this.mWork = work;
    }

    @Override
    protected void onEngineRun() {
        //检测是否有新的任务添加
        mWork.onCheckConnectTask();
        //清除要结束的任务
        mWork.onCheckRemoverTask();

        if (mExecutor.getLoopState() && mWork.getConnectCache().isEmpty() && mWork.getDestroyCache().isEmpty()) {
            mExecutor.waitTask(100);
        }
    }

    @Override
    protected void onDestroyTask() {
        mWork.onRecoveryTaskAll();
    }
}
