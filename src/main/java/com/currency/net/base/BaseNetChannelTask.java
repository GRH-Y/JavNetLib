package com.currency.net.base;

import java.nio.channels.NetworkChannel;

public class BaseNetChannelTask<T extends NetworkChannel> extends BaseTLSTask {

    protected NetworkChannel mChannel;

    protected void setChannel(T channel) {
        this.mChannel = channel;
    }

    protected T getChannel() {
        return (T) mChannel;
    }

    /**
     * 连接失败回调
     */
    protected void onErrorChannel(Throwable throwable) {
    }

    /**
     * 配置Channel
     *
     * @param channel
     */
    protected void onConfigChannel(T channel) {

    }

    /**
     * channel准备就绪,可以正常使用
     *
     * @param channel
     */
    protected void onBeReadyChannel(T channel) {

    }

    /**
     * 准备断开链接回调
     */
    protected void onCloseChannel() {
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        mChannel = null;
    }
}
