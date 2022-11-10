package com.jav.net.nio;

import com.jav.net.base.BaseNetChannelTask;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.state.joggle.IStateMachine;

import java.nio.channels.NetworkChannel;

public class BaseNioChannelTask<T extends NetworkChannel> extends BaseNetChannelTask<T> {

    @Override
    protected void onCreateSSLContext(ISSLComponent sslFactory) {
        super.onCreateSSLContext(sslFactory);
    }

    @Override
    protected IStateMachine getStatusMachine() {
        return super.getStatusMachine();
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
