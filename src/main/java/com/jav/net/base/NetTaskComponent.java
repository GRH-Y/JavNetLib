package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.INetTaskComponentListener;
import com.jav.net.entity.FactoryContext;
import com.jav.net.state.joggle.IStateMachine;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * net task Component,Provide task cache
 *
 * @param <T> net task entity
 * @author yyz
 */
public class NetTaskComponent<T extends BaseNetTask> implements INetTaskComponent<T> {

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

    protected FactoryContext mCreate;
    protected FactoryContext mDestroy;

    private INetTaskComponentListener mListener;


    public NetTaskComponent(FactoryContext context) {
        if (context == null) {
            throw new NullPointerException("FactoryContext can not be null !!!");
        }
        mCreate = context;
        mConnectCache = new ConcurrentLinkedQueue<>();
        mDestroyCache = new ConcurrentLinkedQueue<>();
    }

    public void setDestroyFactoryContext(FactoryContext destroy) {
        this.mDestroy = destroy;
    }

    @Override
    public boolean addExecTask(T task) {
        if (task == null) {
            LogDog.e("## NetTaskComponent addExecTask add task fails, task == null !!!");
            return false;
        }
        IStateMachine<Integer> stateMachine = task.getStatusMachine();
        if (stateMachine.getStatus() != NetTaskStatus.NONE) {
            LogDog.e("## NetTaskComponent addExecTask add task fails, status code = " + stateMachine.getStatus());
            return false;
        }
        AbsNetEngine netEngine = mCreate.getNetEngine();
        if (netEngine.isEngineStoping()) {
            LogDog.e("## NetTaskComponent addExecTask netEngine not running !");
            return false;
        }
        if (mConnectCache.contains(task)) {
            LogDog.e("## NetTaskComponent addExecTask connect cache repeat !");
            return false;
        }
        boolean ret = stateMachine.updateState(NetTaskStatus.NONE, NetTaskStatus.LOAD);
        if (ret) {
            ret = mConnectCache.offer(task);
            if (ret) {
                netEngine.resumeEngine();
                if (mListener != null) {
                    mListener.onAppendChange(true);
                }
            }
        }
        if (!ret) {
            stateMachine.setStatus(NetTaskStatus.INVALID);
            LogDog.e("## NetTaskComponent addExecTask offer fails !");
        }
        return ret;
    }

    @Override
    public int addUnExecTask(T task) {
        int retCode = UN_EXEC_FALSE;
        if (task == null) {
            return retCode;
        }
        IStateMachine<Integer> stateMachine = task.getStatusMachine();
        if (stateMachine.getStatus() < NetTaskStatus.LOAD) {
            return retCode;
        }
        AbsNetEngine netEngine;
        if (mDestroy == null) {
            netEngine = mCreate.getNetEngine();
        } else {
            netEngine = mDestroy.getNetEngine();
        }
        if (netEngine.isEngineStoping()) {
            return retCode;
        }
        if (stateMachine.updateState(NetTaskStatus.LOAD, NetTaskStatus.INVALID)) {
            // 当前任务还在load阶段
            boolean ret = mConnectCache.remove(task);
            if (ret) {
                LogDog.w("## NetTaskComponent remove load status task !!!");
                return UN_EXEC_SUCCESS;
            }
        }
        if (stateMachine.getStatus() <= NetTaskStatus.FINISHING) {
            // 当前任务已经进入finish阶段
            return UN_EXEC_DELAY_SUCCESS;
        }
        // 当前任务处于run阶段
        if (!mDestroyCache.contains(task)) {
            boolean ret = mDestroyCache.offer(task);
            if (ret) {
                netEngine.resumeEngine();
                retCode = UN_EXEC_DELAY_SUCCESS;
                if (mListener != null) {
                    mListener.onAppendChange(false);
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

    public void setListener(INetTaskComponentListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void clearAllQueue() {
        mConnectCache.clear();
        mDestroyCache.clear();
    }

}
