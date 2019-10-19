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
    protected void onCheckConnectTask(boolean isConnectAll) {
        //检测是否有新的任务添加
        while (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            onConnectTask(task);
            if (!isConnectAll) {
                break;
            }
        }
    }

    protected void onExecuteTask() {
    }

    /**
     * 检查要移除任务
     */
    protected void onCheckRemoverTask(boolean isRemoveAll) {
        //销毁链接
        while (!mDestroyCache.isEmpty()) {
            T task = mDestroyCache.remove();
            try {
                onDisconnectTask(task);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (!isRemoveAll) {
                break;
            }
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

    protected void addConnectTask(T task) {
        if (task != null && !mConnectCache.contains(task)) {
            mConnectCache.add(task);
        }
    }

    protected void addDestroyTask(T task) {
        if (task != null && !mDestroyCache.contains(task)) {
            mDestroyCache.add(task);
        }
    }
}
