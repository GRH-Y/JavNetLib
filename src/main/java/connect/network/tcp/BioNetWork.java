package connect.network.tcp;

import connect.network.base.BaseNetTask;
import connect.network.base.BaseNetWork;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BioNetWork<T extends BaseNetTask> extends BaseNetWork<T> {

    /**
     * 正在执行任务的队列
     */
    protected Queue<T> mExecutorQueue;

    protected BioNetWork() {
        mExecutorQueue = new ConcurrentLinkedQueue<>();
    }

    protected Queue<T> getExecutorQueue() {
        return mExecutorQueue;
    }

    @Override
    protected Queue<T> getConnectCache() {
        return super.getConnectCache();
    }

    @Override
    protected Queue<T> getDestroyCache() {
        return super.getDestroyCache();
    }


    //------------------------------------------------------------------------------------------------------------------


    @Override
    protected void onExecuteTask() {
        super.onExecuteTask();
    }

    @Override
    protected void onCheckConnectTask(boolean isConnectAll) {
        //检测是否有新的任务添加
        while (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            onConnectTask(task);
            if (!task.isTaskNeedClose()) {
                mExecutorQueue.add(task);
            }
            if (!isConnectAll) {
                break;
            }
        }
    }


    @Override
    protected void onCheckRemoverTask(boolean isRemoveAll) {
        //销毁链接
        while (!mDestroyCache.isEmpty()) {
            T task = mDestroyCache.remove();
            try {
                onDisconnectTask(task);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            mExecutorQueue.remove(task);
            if (!isRemoveAll) {
                break;
            }
        }
    }

    @Override
    protected void onRecoveryTaskAll() {
        //把所有任务加入销毁队列
        mDestroyCache.addAll(mConnectCache);
        //清除要结束的任务
        onCheckRemoverTask(true);
        mConnectCache.clear();
        mExecutorQueue.clear();
        mDestroyCache.clear();
    }
}
