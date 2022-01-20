package com.currency.net.nio;


import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;

public class BaseNioSelectionTask<T extends NetworkChannel> extends BaseNioChannelTask<T> {

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
        mSelectionKey = null;
    }
}
