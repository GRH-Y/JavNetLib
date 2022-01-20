package com.currency.net.nio;

import com.currency.net.base.BaseNetChannelTask;
import com.currency.net.base.NetTaskStatus;
import com.currency.net.base.joggle.ISSLFactory;

import java.nio.channels.NetworkChannel;

public class BaseNioChannelTask<T extends NetworkChannel> extends BaseNetChannelTask<T> {

    @Override
    protected void onCreateSSLContext(ISSLFactory sslFactory) {
        super.onCreateSSLContext(sslFactory);
    }

    @Override
    protected void setTaskStatus(NetTaskStatus... status) {
        super.setTaskStatus(status);
    }

    @Override
    protected void addTaskStatus(NetTaskStatus... status) {
        super.addTaskStatus(status);
    }

    @Override
    protected void waitAndSetTaskStatus(NetTaskStatus waitStatus, NetTaskStatus setStatus) {
        super.waitAndSetTaskStatus(waitStatus, setStatus);
    }

    @Override
    protected void delTaskStatus(NetTaskStatus... status) {
        super.delTaskStatus(status);
    }

    @Override
    protected void setChannel(T channel) {
        super.setChannel(channel);
    }

    @Override
    protected T getChannel() {
        return super.getChannel();
    }

    @Override
    protected void onConfigChannel(T channel) {
        super.onConfigChannel(channel);
    }

    @Override
    protected void onBeReadyChannel(T channel) {
        super.onBeReadyChannel(channel);
    }

    @Override
    protected void onErrorChannel(Throwable throwable) {
        super.onErrorChannel(throwable);
    }

    @Override
    protected void onCloseChannel() {
        super.onCloseChannel();
    }
}
