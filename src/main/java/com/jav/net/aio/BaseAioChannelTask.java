package com.jav.net.aio;

import com.jav.net.base.BaseTlsTask;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.base.joggle.NetErrorType;

import java.nio.channels.NetworkChannel;

public class BaseAioChannelTask<T extends NetworkChannel> extends BaseTlsTask<T> {

    @Override
    protected void onCreateSSLContext(ISSLComponent sslFactory) {
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
    protected void onErrorChannel(NetErrorType errorType, Throwable throwable) {
        super.onErrorChannel(errorType, throwable);
    }

    @Override
    protected void onCloseChannel() {
        super.onCloseChannel();
    }
}
