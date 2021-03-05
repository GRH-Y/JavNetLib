package connect.network.tcp;

import connect.network.base.AbsNetEngine;
import connect.network.base.BaseNetTask;

/**
 * 工厂的核心类
 *
 * @param <T>
 */
public class BioEngine<T extends BaseNetTask> extends AbsNetEngine {


    protected BioNetWork<T> mWork;


    public BioEngine(BioNetWork<T> work) {
        this.mWork = work;
    }

    @Override
    protected void onEngineRun() {
        //检测是否有新的任务添加
        mWork.onCheckConnectTask();
        //检查是否有任务
        mWork.onExecuteTask();
        //清除要结束的任务
        mWork.onCheckRemoverTask();

        if (mExecutor.isLoopState()) {
            if (mWork.getExecutorQueue().isEmpty() && mWork.getConnectCache().isEmpty() && mWork.getDestroyCache().isEmpty()) {
                mExecutor.waitTask(0);
            } else {
                mExecutor.waitTask(100);
            }
        }
    }

    @Override
    protected void onDestroyTask() {
        mWork.onRecoveryTaskAll();
    }
}
