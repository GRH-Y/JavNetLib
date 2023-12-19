package com.jav.net.base;

import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.joggle.NetErrorType;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;

/**
 * 基本网络任务,创建网络链接通信
 *
 * @author yyz
 */
public class BaseNetTask<C extends NetworkChannel> {

    /**
     * 目标地址
     */
    private String mHost = null;

    /**
     * 目标端口
     */
    private int mPort = -1;

    /**
     * 事件选择键
     */
    private SelectionKey mSelectionKey;


    /**
     * 网络通道
     */
    private C mChannel;

    /**
     * 任务状态机
     *
     * @see NetTaskStatus 网络状态
     */
    private final NetStateMachine mStatusMachine;


    public BaseNetTask() {
        mStatusMachine = new NetStateMachine(new NetTaskStatus());

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


    protected void setChannel(C channel) {
        this.mChannel = channel;
    }

    protected void setSelectionKey(SelectionKey key) {
        this.mSelectionKey = key;
    }

    protected C getChannel() {
        return mChannel;
    }

    protected SelectionKey getSelectionKey() {
        return mSelectionKey;
    }


    // -----------------------------------call back-------------------------------------------------------


    /**
     * 配置Channel
     *
     * @param channel 通道
     */
    protected void onConfigChannel(C channel) {
    }

    /**
     * channel准备就绪,可以正常使用
     *
     * @param channel 通道
     */
    protected void onBeReadyChannel(SelectionKey selectionKey, C channel) {

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
    }
}
