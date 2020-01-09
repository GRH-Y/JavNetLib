package connect.network.tcp;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetTask;

/**
 * 工厂的核心类
 *
 * @param <T>
 */
public class BioEngine<T extends BaseNetTask> extends AbsNetEngine {


    protected AbsNetFactory<T> mFactory;

    protected BioNetWork<T> mWork;


    public BioEngine(AbsNetFactory<T> factory, BioNetWork<T> work) {
        this.mFactory = factory;
        this.mWork = work;
    }

    @Override
    protected void onEngineRun() {
        //检测是否有新的任务添加
        mWork.onCheckConnectTask();
        //检查是否有读写任务
        mWork.onExecuteTask();
        //清除要结束的任务
        mWork.onCheckRemoverTask();

        if (mExecutor.getLoopState() && mWork.getExecutorQueue().isEmpty() && mWork.getConnectCache().isEmpty()
                && mWork.getDestroyCache().isEmpty()) {
            mExecutor.waitTask(0);
        }
    }


    @Override
    protected void onDestroyTask() {
        mWork.onRecoveryTaskAll();
    }
}
