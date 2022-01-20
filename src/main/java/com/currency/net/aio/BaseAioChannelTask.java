package com.currency.net.aio;

import com.currency.net.base.BaseNetChannelTask;
import com.currency.net.base.joggle.ISSLFactory;

import java.nio.channels.NetworkChannel;

public class BaseAioChannelTask<T extends NetworkChannel> extends BaseNetChannelTask<T> {

    @Override
    protected void onCreateSSLContext(ISSLFactory sslFactory) {
        super.onCreateSSLContext(sslFactory);
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
