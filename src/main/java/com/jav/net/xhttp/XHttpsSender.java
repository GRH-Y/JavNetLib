package com.jav.net.xhttp;

import com.jav.net.base.AbsNetSender;
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
    protected int sendDataImp(ByteBuffer[] buffers) throws Throwable {
        mTLSHandler.wrapAndWrite(mChannel, buffers);
        return AbsNetSender.SEND_COMPLETE;
    }

}
