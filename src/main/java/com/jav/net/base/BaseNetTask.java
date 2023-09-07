package com.jav.net.base;

import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.common.state.joggle.IStateChangeListener;
import com.jav.common.util.StringEnvoy;
import com.jav.net.base.joggle.NetErrorType;

import java.nio.channels.NetworkChannel;

/**
 * 基本网络任务,创建网络链接通信
 *
 * @author yyz
 */
public class BaseNetTask<T extends NetworkChannel> {

    /**
     * 目标地址
     */
    protected String mHost = null;

    /**
     * 目标端口
     */
    protected int mPort = -1;


    /**
     * 网络通道
     */
    protected T mChannel;

    /**
     * 任务状态机
     *
     * @see NetTaskStatus 网络状态
     */
    private final NetStateMachine mStatusMachine;


    /**
     * 状态监听器
     */
    private final IStateChangeListener<Integer> mListener = this::onTaskState;


    public BaseNetTask() {
        mStatusMachine = new NetStateMachine(new NetTaskStatus());
        mStatusMachine.regStateChangeListener(mListener);
    }

    protected IControlStateMachine<Integer> getStatusMachine() {
        return mStatusMachine;
    }

    /**
     * 设置链接目标信息
     *
     * @param host 目标地址
     * @param port 目标端口
     */
    public void setAddress(String host, int port) {
        if (StringEnvoy.isEmpty(host) || port < 0) {
            throw new IllegalStateException("host or port is invalid !!! ");
        }
        this.mHost = host;
        this.mPort = port;
    }

    /**
     * 获取目标的端口
     *
     * @return
     */
    public int getPort() {
        return mPort;
    }

    /**
     * 获取目标的地址
     *
     * @return
     */
    public String getHost() {
        return mHost;
    }


    protected void setChannel(T channel) {
        this.mChannel = channel;
    }

    protected T getChannel() {
        return mChannel;
    }


    // -----------------------------------call back-------------------------------------------------------


    /**
     * 任务状态变化回调
     *
     * @param statCode
     */
    protected void onTaskState(int statCode) {
    }


    /**
     * 配置Channel
     *
     * @param channel 通道
     */
    protected void onConfigChannel(T channel) {

    }

    /**
     * channel准备就绪,可以正常使用
     *
     * @param channel 通道
     */
    protected void onBeReadyChannel(T channel) {

    }

    /**
     * 准备断开链接回调
     */
    protected void onCloseChannel() {
    }


    /**
     * 连接失败回调
     */
    protected void onErrorChannel(NetErrorType errorType, Throwable throwable) {
    }


    /**
     * 当前状态链接彻底关闭，可以做资源回收工作
     */
    protected void onRecovery() {
        mStatusMachine.unRegStateChangeListener(mListener);
        mHost = null;
        mPort = -1;
        mChannel = null;
    }
}
