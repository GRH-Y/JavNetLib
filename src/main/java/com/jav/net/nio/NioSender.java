package com.jav.net.nio;


import com.jav.net.entity.MultiByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioSender extends AbsNioCacheNetSender<MultiByteBuffer> {

    protected SocketChannel mChannel;

    public void setChannel(SelectionKey selectionKey, SocketChannel channel) {
        if (selectionKey == null || channel == null) {
            throw new NullPointerException("selectionKey or channel is null !!!");
        }
        this.mSelectionKey = selectionKey;
        this.mChannel = channel;
    }


    @Override
    protected int sendDataImp(ByteBuffer[] buffers) throws Throwable {
        if (mChannel == null || buffers == null || !mChannel.isConnected()) {
            return SEND_FAIL;
        }
        do {
            long ret = mChannel.write(buffers);
            if (ret < 0) {
                throw new IOException("## failed to send data. The socket channel may be closed !!! ");
            } else if (ret == 0 && mChannel.isConnected()) {
                return hasRemaining(buffers) ? SEND_CHANNEL_BUSY : SEND_FAIL;
            }
        } while (hasRemaining(buffers) && mChannel.isConnected());
        return SEND_COMPLETE;
    }
}
