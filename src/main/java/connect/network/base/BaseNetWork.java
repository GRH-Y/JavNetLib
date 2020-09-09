package connect.network.base;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseNetWork<T extends BaseNetTask> {

    /**
     * 等待创建连接队列
     */
    protected Queue<T> mConnectCache;

    /**
     * 销毁任务队列
     */
    protected Queue<T> mDestroyCache;

    public BaseNetWork() {
        mConnectCache = new ConcurrentLinkedQueue<>();
        mDestroyCache = new ConcurrentLinkedQueue<>();
    }

    protected Queue<T> getConnectCache() {
        return mConnectCache;
    }

    protected Queue<T> getDestroyCache() {
        return mDestroyCache;
    }

    //------------------------------------------------------------------------------------

    /**
     * 检查要链接任务
     */
    protected void onCheckConnectTask() {
        //检测是否有新的任务添加
        if (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            onConnectTask(task);
        }
    }

    protected void onExecuteTask() {
    }

    /**
     * 检查要移除任务
     */
    protected void onCheckRemoverTask() {
        //销毁链接
        if (!mDestroyCache.isEmpty()) {
            T task = mDestroyCache.remove();
            onDisconnectTask(task);
        }
    }

    //------------------------------------------------------------------------------------

    /**
     * 准备链接
     *
     * @param task 网络请求任务
     */
    protected void onConnectTask(T task) {
    }

    /**
     * 准备断开链接回调
     *
     * @param task 网络请求任务
     */
    protected void onDisconnectTask(T task) {
    }


    /**
     * 断开链接后回调
     *
     * @param task 网络请求任务
     */
    protected void onRecoveryTask(T task) {
        try {
            task.reset();
            task.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 销毁所有的链接任务
     */
    protected void onRecoveryTaskAll() {
    }

    //------------------------------------------------------------------------------------

    protected boolean addConnectTask(T task) {
        boolean ret = false;
        if (task != null && !mConnectCache.contains(task)) {
            ret = mConnectCache.add(task);
        }
        return ret;
    }

    protected boolean addDestroyTask(T task) {
        boolean ret = false;
        if (task != null && !mDestroyCache.contains(task)) {
            task.setTaskNeedClose(true);
            ret = mDestroyCache.add(task);
        }
        return ret;
    }
}
