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
    protected void onCheckConnectTask() {
        //检测是否有新的任务添加
        while (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            onConnectTask(task);
            if (!task.isTaskNeedClose()) {
                mExecutorQueue.add(task);
            }
        }
    }


    @Override
    protected void onCheckRemoverTask() {
        //销毁链接
        if (!mDestroyCache.isEmpty()) {
            T task = mDestroyCache.remove();
            try {
                onDisconnectTask(task);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            mExecutorQueue.remove(task);
        }
    }

    @Override
    protected void onRecoveryTaskAll() {
        for (T task : mExecutorQueue) {
            try {
                onDisconnectTask(task);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        mConnectCache.clear();
        mExecutorQueue.clear();
        mDestroyCache.clear();
    }

    @Override
    protected boolean addConnectTask(T task) {
        boolean ret = false;
        if (task != null && !mConnectCache.contains(task) && !mExecutorQueue.contains(task)) {
            ret = mConnectCache.add(task);
        }
        return ret;
    }

    @Override
    protected void addDestroyTask(T task) {
        if (task != null && !mDestroyCache.contains(task) && mExecutorQueue.contains(task)) {
            mDestroyCache.add(task);
        }
    }
}
