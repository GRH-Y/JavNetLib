package connect.network.aio;

import connect.network.base.AbsNetEngine;

public class AioEngine extends AbsNetEngine {

    protected AioClientNetWork mWork;

    public AioEngine(AioClientNetWork work) {
        this.mWork = work;
    }

    @Override
    protected void onEngineRun() {
        //检测是否有新的任务添加
        mWork.onCheckConnectTask();
        //清除要结束的任务
        mWork.onCheckRemoverTask();

        if (mExecutor.isLoopState() && mWork.getConnectCache().isEmpty() && mWork.getDestroyCache().isEmpty()) {
            //如果没有任务则休眠
            mExecutor.waitTask(0);
        }
    }

    @Override
    protected void onDestroyTask() {
        mWork.onRecoveryTaskAll();
    }
}
