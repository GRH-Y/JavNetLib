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

    public Queue<T> getConnectCache() {
        return mConnectCache;
    }

    public Queue<T> getDestroyCache() {
        return mDestroyCache;
    }

    //------------------------------------------------------------------------------------

    /**
     * 检查要链接任务
     */
    protected void onCheckConnectTask() {
        //检测是否有新的任务添加
        T task = mConnectCache.poll();
        if (task != null) {
            connectImp(task);
        }
    }

    protected void connectImp(T task) {
        task.changeTaskStatus(TaskStatus.RUN);
        onConnectTask(task);
    }

    protected void onExecuteTask() {
    }

    /**
     * 检查要移除任务
     */
    protected void onCheckRemoverTask() {
        T task = mDestroyCache.poll();
        if (task != null) {
            removerTaskImp(task);
        }
    }

    protected void removerTaskImp(T task) {
        task.changeTaskStatus(TaskStatus.NONE);
        onDisconnectTask(task);
    }


    /**
     * 销毁所有的链接任务
     */
    protected void onRecoveryTaskAll() {
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


    //------------------------------------------------------------------------------------

    protected boolean addConnectTask(T task) {
        boolean ret = false;
        if (task != null && !mConnectCache.contains(task)) {
            task.changeTaskStatus(TaskStatus.USE);
            ret = mConnectCache.offer(task);
            if (!ret) {
                task.changeTaskStatus(TaskStatus.NONE);
            }
        }
        return ret;
    }

    protected boolean addDestroyTask(T task) {
        boolean ret = false;
        if (task != null && !mDestroyCache.contains(task)) {
            ret = mDestroyCache.offer(task);
        }
        return ret;
    }
}
