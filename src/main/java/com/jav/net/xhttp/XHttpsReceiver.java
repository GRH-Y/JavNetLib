package com.jav.net.xhttp;

import com.jav.net.entity.MultiByteBuffer;
import com.jav.net.nio.NioReceiver;
import com.jav.net.ssl.TLSHandler;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class XHttpsReceiver extends NioReceiver {

    protected TLSHandler mTLSHandler;

    public XHttpsReceiver(TLSHandler tlsHandler) {
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.mTLSHandler = tlsHandler;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.mTLSHandler = tlsHandler;
    }


    @Override
    protected void onReadImp(SocketChannel channel, MultiByteBuffer newBuffer) throws Throwable {
        ByteBuffer[] cacheData = null;
        int ret;
        try {
            do {
                cacheData = newBuffer.getAllBuf();
                ret = mTLSHandler.readAndUnwrap(channel, false, cacheData);
                if (ret == TLSHandler.NOT_ENOUGH_CAPACITY) {
                    // 解码缓存容量不够，则需要多传byteBuffer
                    newBuffer.setBackBuf(cacheData);
                    newBuffer.appendBuffer();
                }
            } while (ret == TLSHandler.NOT_ENOUGH_CAPACITY);
        } catch (Throwable e) {
            throw e;
        } finally {
            newBuffer.setBackBuf(cacheData);
            newBuffer.flip();
        }
    }
}
