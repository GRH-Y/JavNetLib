package com.currency.net.base;

import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.entity.FactoryContext;
import com.currency.net.entity.NetTaskStatusCode;
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

    private final AtomicBoolean isEnable = new AtomicBoolean(true);


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
        if (!isEnable.get() || task == null) {
            return false;
        }
        if (!task.getTaskStatus().equals(NetTaskStatusCode.NONE)) {
            return false;
        }
        AbsNetEngine netEngine = mContext.getNetEngine();
        if (!netEngine.isEngineRunning()) {
            return false;
        }
        boolean ret = false;
        if (!mConnectCache.contains(task)) {
            ret = mConnectCache.offer(task);
            if (ret) {
                ret = task.updateTaskStatus(NetTaskStatusCode.NONE, NetTaskStatusCode.LOAD);
                if (ret) {
                    netEngine.resumeEngine();
                } else {
                    mConnectCache.remove(task);
                }
            }
        }
        return ret;
    }

    @Override
    public int addUnExecTask(T task) {
        int retCode = UN_EXEC_FALSE;
        if (!isEnable.get() || task == null) {
            return retCode;
        }
        if (task.getTaskStatus().getCode() < NetTaskStatusCode.LOAD.getCode()) {
            return retCode;
        }
        AbsNetEngine netEngine = mContext.getNetEngine();
        if (!netEngine.isEngineRunning()) {
            return retCode;
        }
        if (task.getTaskStatus().equals(NetTaskStatusCode.LOAD)) {
            boolean ret = mConnectCache.remove(task);
            if (ret) {
                LogDog.w("## NetTaskComponent remove load status task !!!");
                return UN_EXEC_SUCCESS;
            }
        }
        if (!mDestroyCache.contains(task)) {
            boolean ret = mDestroyCache.offer(task);
            if (ret) {
                netEngine.resumeEngine();
                retCode = UN_EXEC_DELAY_SUCCESS;
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
