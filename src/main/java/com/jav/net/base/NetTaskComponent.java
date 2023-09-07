package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.joggle.INetTaskComponent;

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
        LogDog.w("------> new NetTaskComponent " + this);
    }

    public void setDestroyFactoryContext(FactoryContext destroy) {
        this.mDestroy = destroy;
    }

    @Override
    public boolean addExecTask(T task) {
        if (task == null) {
            LogDog.e("## addExecTask add task fails, task == null !!!");
            return false;
        }
        IControlStateMachine<Integer> stateMachine = task.getStatusMachine();
        if (stateMachine.getState() != NetTaskStatus.NONE) {
            LogDog.e("## addExecTask add task fails, status code = " + stateMachine.getState());
            return false;
        }
        BaseNetEngine netEngine = mCreate.getNetEngine();
        if (netEngine.isEngineStop()) {
            LogDog.e("## addExecTask netEngine not running !");
            return false;
        }
        if (mConnectCache.contains(task)) {
            LogDog.e("## addExecTask connect cache repeat !");
            return false;
        }
        boolean ret = stateMachine.updateState(NetTaskStatus.NONE, NetTaskStatus.LOAD);
        if (!ret) {
            return false;
        }
        ret = mConnectCache.offer(task);
        if (ret) {
            netEngine.resumeEngine();
//            LogDog.i("## addExecTask  task = " + task + " component = " + this);
        } else {
            LogDog.e("## addExecTask offer fails !");
        }
        return ret;
    }


    @Override
    public boolean addUnExecTask(T task) {
        if (task == null) {
            LogDog.e("## addUnExecTask add task fails, task == null !!!");
            return false;
        }
        IControlStateMachine<Integer> stateMachine = task.getStatusMachine();
        if (stateMachine.getState() < NetTaskStatus.LOAD) {
            LogDog.e("## addUnExecTask add task fails, status code = " + stateMachine.getState());
            return false;
        }
        BaseNetEngine netEngine;
        if (mDestroy == null) {
            netEngine = mCreate.getNetEngine();
        } else {
            netEngine = mDestroy.getNetEngine();
        }
        if (netEngine.isEngineStop()) {
            LogDog.e("## addUnExecTask netEngine not running !");
            return false;
        }
        boolean ret;
        if (mDestroyCache.contains(task)) {
            LogDog.e("## addUnExecTask destroy cache repeat , status = " + stateMachine.getState() + " task = " + task);
            return false;
        }
        // attach FINISHING
        for (; !stateMachine.isAttachState(NetTaskStatus.FINISHING); ) {
            int state = stateMachine.getState();
            if (state == NetTaskStatus.INVALID) {
                // 其他线程修改了状态
                return false;
            } else if (state == NetTaskStatus.LOAD) {
                //任务状态还没开始执行，直接切换为INVALID状态
                if (stateMachine.updateState(NetTaskStatus.LOAD, NetTaskStatus.INVALID)) {
                    return true;
                }
            } else {
                if (stateMachine.attachState(NetTaskStatus.FINISHING)) {
                    break;
                }
            }
        }
        ret = mDestroyCache.offer(task);
        if (ret) {
            netEngine.resumeEngine();
//            LogDog.i("## addUnExecTask  task = " + task + " netEngine = " + netEngine + " component = " + this);
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
