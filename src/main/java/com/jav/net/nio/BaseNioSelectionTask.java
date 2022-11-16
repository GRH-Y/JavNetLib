package com.jav.net.nio;


import com.jav.common.state.joggle.IStateMachine;
import com.jav.net.base.BaseTlsTask;
import com.jav.net.base.joggle.ISSLComponent;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;


/**
 * 基本nio channel的task
 *
 * @param <T>
 * @author yyz
 */
public class BaseNioSelectionTask<T extends NetworkChannel> extends BaseTlsTask<T> {


    protected SelectionKey mSelectionKey;

    protected void setSelectionKey(SelectionKey selectionKey) {
        this.mSelectionKey = selectionKey;
    }

    protected SelectionKey getSelectionKey() {
        return mSelectionKey;
    }


    @Override
    protected void onRecovery() {
        super.onRecovery();
        if (mSelectionKey != null) {
            mSelectionKey.attach(null);
            mSelectionKey = null;
        }
    }

    @Override
    protected <M extends IStateMachine> M getStatusMachine() {
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
    protected void onCreateSSLContext(ISSLComponent sslFactory) {
        super.onCreateSSLContext(sslFactory);
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
