package com.currency.net.nio;

import com.currency.net.base.BaseNetChannelTask;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.entity.NetTaskStatusCode;

import java.nio.channels.NetworkChannel;

public class BaseNioChannelTask<T extends NetworkChannel> extends BaseNetChannelTask<T> {

    @Override
    protected void onCreateSSLContext(ISSLFactory sslFactory) {
        super.onCreateSSLContext(sslFactory);
    }

    @Override
    public boolean updateTaskStatus(NetTaskStatusCode expectStatus, NetTaskStatusCode setStatus) {
        return super.updateTaskStatus(expectStatus, setStatus);
    }

    @Override
    protected boolean updateTaskStatus(NetTaskStatusCode expectStatus, NetTaskStatusCode setStatus, boolean isWait) {
        return super.updateTaskStatus(expectStatus, setStatus, isWait);
    }

    @Override
    public NetTaskStatusCode getTaskStatus() {
        return super.getTaskStatus();
    }

    @Override
    protected void setTaskStatus(NetTaskStatusCode newStatus) {
        super.setTaskStatus(newStatus);
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
