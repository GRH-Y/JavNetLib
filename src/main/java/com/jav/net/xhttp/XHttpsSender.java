package com.jav.net.xhttp;

import com.jav.net.nio.NioSender;
import com.jav.net.ssl.TLSHandler;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class XHttpsSender extends NioSender {

    protected TLSHandler mTLSHandler;

    public XHttpsSender(TLSHandler tlsHandler, SelectionKey selectionKey, SocketChannel channel) {
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.mTLSHandler = tlsHandler;
        setChannel(selectionKey, channel);
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.mTLSHandler = tlsHandler;
    }

    @Override
    protected int sendDataImp(Object data) {
        ByteBuffer[] buffers = null;
        if (data instanceof ByteBuffer[]) {
            buffers = (ByteBuffer[]) data;
        }
        if (buffers == null) {
            return SEND_FAIL;
        }
        try {
            mTLSHandler.wrapAndWrite(mChannel, buffers);
        } catch (Throwable e) {
            return SEND_FAIL;
        }
        return SEND_COMPLETE;
    }

}
