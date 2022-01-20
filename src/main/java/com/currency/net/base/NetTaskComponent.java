package com.currency.net.base;

import com.currency.net.base.joggle.INetTaskContainer;
import log.LogDog;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;


public class NetTaskComponent<T extends BaseNetTask> implements INetTaskContainer<T> {

    public static final int UN_EXEC_SUCCESS = 0;
    public static final int UN_EXEC_DELAY_SUCCESS = 1;
    public static final int UN_EXEC_FALSE = 2;

    /**
     * 等待创建连接队列
     */
    protected Queue<T> mConnectCache;

    /**
     * 销毁任务队列
     */
    protected Queue<T> mDestroyCache;

    protected FactoryContext mContext;

    private volatile AtomicBoolean isEnable = new AtomicBoolean(true);


    public NetTaskComponent(FactoryContext context) {
        if (context == null) {
            throw new NullPointerException("FactoryContext can not be null !!!");
        }
        mContext = context;
        mConnectCache = new ConcurrentLinkedQueue<>();
        mDestroyCache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public boolean addExecTask(T task) {
        if (isEnable.get() == false || task == null) {
            return false;
        }
        boolean ret = false;
        if (task.isHasStatus(NetTaskStatus.READY_END) || task.isHasStatus(NetTaskStatus.FINISH)) {
            return false;
        }
        if ((task.getTaskStatus() == NetTaskStatus.NONE.getCode() || task.isHasStatus(NetTaskStatus.ASSIGN)) && isEnable.get()) {
            AbsNetEngine netEngine = mContext.getNetEngine();
            ret = netEngine.isEngineRunning();
            if (ret && isEnable.get()) {
                if (task != null && !mConnectCache.contains(task)) {
                    ret = mConnectCache.offer(task);
                    if (ret && !task.isHasStatus(NetTaskStatus.ASSIGN)) {
                        task.setTaskStatus(NetTaskStatus.LOAD);
                    }
                }
                if (ret) {
                    netEngine.resumeEngine();
                }
            }
        }
        return ret;
    }

    @Override
    public int addUnExecTask(T task) {
        if (isEnable.get() == false || task == null) {
            return UN_EXEC_FALSE;
        }
        if (task.getTaskStatus() == NetTaskStatus.NONE.getCode()) {
            return UN_EXEC_FALSE;
        }
        int retCode = UN_EXEC_FALSE;
        if (task.isHasStatus(NetTaskStatus.LOAD)) {
            boolean ret = mConnectCache.remove(task);
            if (ret) {
                LogDog.w("## NetTaskComponent remove load status task !!!");
                return UN_EXEC_SUCCESS;
            }
        }
        if (!(task.isHasStatus(NetTaskStatus.READY_END) || task.isHasStatus(NetTaskStatus.FINISH))) {
            AbsNetEngine netEngine = mContext.getNetEngine();
            if (netEngine.isEngineRunning()) {
                if (!mDestroyCache.contains(task)) {
                    boolean ret = mDestroyCache.offer(task);
                    if (ret) {
                        task.addTaskStatus(NetTaskStatus.READY_END);
                        netEngine.resumeEngine();
                        retCode = UN_EXEC_DELAY_SUCCESS;
                    }
                }
            }
        }
        return retCode;
    }

    @Override
    public boolean isConnectQueueEmpty() {
        return mConnectCache.isEmpty();
    }

    @Override
    public boolean isDestroyQueueEmpty() {
        return mDestroyCache.isEmpty();
    }

    @Override
    public T pollConnectTask() {
        return mConnectCache.poll();
    }

    @Override
    public T pollDestroyTask() {
        return mDestroyCache.poll();
    }

    @Override
    public void clearAllQueue() {
        isEnable.set(false);
        mConnectCache.clear();
        mDestroyCache.clear();
    }

}
