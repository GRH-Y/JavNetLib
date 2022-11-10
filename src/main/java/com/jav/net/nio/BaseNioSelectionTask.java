package com.jav.net.nio;


import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;

/**
 * 带有SelectionKey的task
 *
 * @param <T>
 * @author yyz
 */
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
        if (mSelectionKey != null) {
            mSelectionKey.attach(null);
            mSelectionKey = null;
        }
    }
}
