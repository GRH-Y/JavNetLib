package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.entity.FactoryContext;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * net task Component,Provide task cache
 *
 * @param <T> net task entity
 * @author yyz
 */
public class NetTaskComponent<T extends BaseNetTask> implements INetTaskComponent<T> {

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
        IControlStateMachine<Integer> stateMachine = task.getStatusMachine();
        if (stateMachine.getState() != NetTaskStatus.NONE) {
            LogDog.e("## NetTaskComponent addExecTask add task fails, status code = " + stateMachine.getState());
            return false;
        }
        AbsNetEngine netEngine = mCreate.getNetEngine();
        if (netEngine.isEngineStop()) {
            LogDog.e("## NetTaskComponent addExecTask netEngine not running !");
            return false;
        }
        if (mConnectCache.contains(task)) {
            LogDog.e("## NetTaskComponent addExecTask connect cache repeat !");
            return false;
        }
        boolean ret = stateMachine.updateState(NetTaskStatus.NONE, NetTaskStatus.LOAD);
        if (!ret) {
            return false;
        }
        ret = mConnectCache.offer(task);
        if (ret) {
            netEngine.resumeEngine();
        } else {
            stateMachine.updateState(NetTaskStatus.LOAD, NetTaskStatus.INVALID);
            LogDog.e("## NetTaskComponent addExecTask offer fails !");
        }
        return ret;
    }

    @Override
    public boolean addUnExecTask(T task) {
        if (task == null) {
            LogDog.e("## NetTaskComponent addUnExecTask add task fails, task == null !!!");
            return false;
        }
        IControlStateMachine<Integer> stateMachine = task.getStatusMachine();
        if (stateMachine.getState() < NetTaskStatus.LOAD) {
            LogDog.e("## NetTaskComponent addUnExecTask add task fails, status code = " + stateMachine.getState());
            return false;
        } else if (stateMachine.getState() == NetTaskStatus.LOAD) {
            boolean ret = stateMachine.updateState(NetTaskStatus.LOAD, NetTaskStatus.INVALID);
            // 当前任务还在load阶段
            if (ret) {
                ret = mConnectCache.remove(task);
            }
            if (ret) {
                LogDog.w("## NetTaskComponent remove load status task !!!");
                return true;
            }
        }
        AbsNetEngine netEngine;
        if (mDestroy == null) {
            netEngine = mCreate.getNetEngine();
        } else {
            netEngine = mDestroy.getNetEngine();
        }
        if (netEngine.isEngineStop()) {
            LogDog.e("## NetTaskComponent addUnExecTask netEngine not running !");
            return false;
        }
        if (mDestroyCache.contains(task)) {
            LogDog.e("## NetTaskComponent addUnExecTask connect cache repeat !");
            return false;
        }
        if (stateMachine.isAttachState(NetTaskStatus.FINISHING)) {
            // 其他线程修改了状态
            return false;
        }
        while (!stateMachine.attachState(NetTaskStatus.FINISHING)) {
        }
        boolean ret = mDestroyCache.offer(task);
        if (ret) {
            netEngine.resumeEngine();
        }
        return ret;
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
        mConnectCache.clear();
        mDestroyCache.clear();
    }

}
