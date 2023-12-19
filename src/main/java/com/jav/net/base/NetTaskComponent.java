package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.joggle.INetTaskComponent;

/**
 * net task Component,Provide task cache
 *
 * @param <T> net task entity
 * @author yyz
 */
public class NetTaskComponent<T extends BaseNetTask> implements INetTaskComponent<T> {


    private NioDisconnectWork mDisconnectWork;

    private NioConnectWork mConnectWork;


    public NetTaskComponent(FactoryContext context) {
        mConnectWork = new NioConnectWork(context);
        mConnectWork.startWork();
        mDisconnectWork = new NioDisconnectWork(context);
        mDisconnectWork.startWork();
    }


    @Override
    public boolean addExecTask(T task) {
        if (task == null) {
            LogDog.e("#Component# addExecTask add task fails, task == null !!!");
            return false;
        }
        IControlStateMachine<Integer> stateMachine = task.getStatusMachine();
        if (stateMachine.getState() != NetTaskStatus.NONE) {
            LogDog.e("#Component# addExecTask add task fails, status code = " + stateMachine.getState());
            return false;
        }
        if (mConnectWork.isContains(task)) {
            LogDog.e("#Component# addExecTask connect cache repeat !");
            return false;
        }
        boolean ret = stateMachine.updateState(NetTaskStatus.NONE, NetTaskStatus.LOAD);
        if (!ret) {
            return false;
        }
        ret = mConnectWork.pushConnectTask(task);
        if (!ret) {
            LogDog.e("#Component# addExecTask offer fails !");
        }
//        else {
//            LogDog.i("#Component# addExecTask  task : " + task);
//        }
        return ret;
    }


    @Override
    public boolean addUnExecTask(T task) {
        if (task == null) {
            LogDog.e("#Component# addUnExecTask add task fails, task == null !!!");
            return false;
        }
        IControlStateMachine<Integer> stateMachine = task.getStatusMachine();
        if (stateMachine.getState() < NetTaskStatus.LOAD) {
            LogDog.e("#Component# addUnExecTask add task fails, status code = " + stateMachine.getState());
            return false;
        }
        boolean ret;
        if (mDisconnectWork.isContains(task)) {
            LogDog.e("#Component# addUnExecTask destroy cache repeat , status = " + stateMachine.getState() + " task = " + task);
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
        ret = mDisconnectWork.pushDisconnectTask(task);
        if (!ret) {
            LogDog.e("#Component# addUnExecTask fails : " + task);
        }
//        else {
//            LogDog.i("#Component# addUnExecTask  task = " + task);
//        }
        return ret;
    }


    @Override
    public void release() {
        mConnectWork.stopWork();
        mDisconnectWork.stopWork();
    }

}
