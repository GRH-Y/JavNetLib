package connect.network.tcp;

import connect.network.base.BaseNetTask;
import connect.network.base.BaseNetWork;
import connect.network.base.TaskStatus;

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

    //------------------------------------------------------------------------------------------------------------------


    @Override
    protected void onExecuteTask() {
        super.onExecuteTask();
    }

    @Override
    protected void onCheckConnectTask() {
        super.onCheckConnectTask();
    }

    @Override
    protected void connectImp(T task) {
        super.connectImp(task);
        if (task.getTaskStatus() == TaskStatus.RUN) {
            mExecutorQueue.add(task);
        }
    }

    @Override
    protected void onCheckRemoverTask() {
        super.onCheckRemoverTask();
    }

    @Override
    protected void removerTaskImp(T task) {
        super.removerTaskImp(task);
        mExecutorQueue.remove(task);
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
    protected boolean addDestroyTask(T task) {
        boolean ret = false;
        if (task != null && !mDestroyCache.contains(task) && mExecutorQueue.contains(task)) {
            ret = mDestroyCache.add(task);
        }
        return ret;
    }
}
