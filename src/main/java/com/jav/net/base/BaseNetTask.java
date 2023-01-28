package com.jav.net.base;

import com.jav.common.state.joggle.IStateChangeListener;
import com.jav.common.state.joggle.IStateMachine;
import com.jav.common.util.StringEnvoy;
import com.jav.net.base.NetStateMachine;
import com.jav.net.base.NetTaskStatus;

/**
 * 基本网络任务,创建网络链接通信
 *
 * @author yyz
 */
public class BaseNetTask {

    protected String mHost = null;
    protected int mPort = -1;

    private final NetStateMachine mStatusMachine;


    private IStateChangeListener mListener = (IStateChangeListener<Integer>) state -> {
        onTaskState(state);
    };


    public BaseNetTask() {
        mStatusMachine = new NetStateMachine(new NetTaskStatus());
        mStatusMachine.regStateChangeListener(mListener);
    }

    protected <M extends IStateMachine> M getStatusMachine() {
        return (M) mStatusMachine;
    }

    /**
     * 任务状态变化回调
     *
     * @param statCode
     */
    protected void onTaskState(int statCode) {
    }


    public void setAddress(String host, int port) {
        if (StringEnvoy.isEmpty(host) || port < 0) {
            throw new IllegalStateException("host or port is invalid !!! ");
        }
        this.mHost = host;
        this.mPort = port;
    }

    public int getPort() {
        return mPort;
    }

    public String getHost() {
        return mHost;
    }

    /**
     * 当前状态链接彻底关闭，可以做资源回收工作
     */
    protected void onRecovery() {
        mStatusMachine.unRegStateChangeListener(mListener);
        mHost = null;
        mPort = -1;
    }
}
