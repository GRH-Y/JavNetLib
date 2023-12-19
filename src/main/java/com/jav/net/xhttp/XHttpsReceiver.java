package com.jav.net.xhttp;

import com.jav.net.base.MultiBuffer;
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
    protected void onReadImp(SocketChannel channel) throws Throwable {
        ByteBuffer[] cacheData = null;
        Throwable catchException = null;
        MultiBuffer multiBuffer = new MultiBuffer();
        int ret;
        try {
            cacheData = multiBuffer.rentAllBuf();
            do {
                ret = mTLSHandler.readAndUnwrap(channel, false, cacheData);
                if (ret == TLSHandler.NOT_ENOUGH_CAPACITY) {
                    // 解码缓存容量不够，则需要多传byteBuffer
                    multiBuffer.restoredBuf(cacheData);
                    multiBuffer.appendBuffer();
                    cacheData = multiBuffer.rentAllBuf();
                }
            } while (ret == TLSHandler.NOT_ENOUGH_CAPACITY);
        } catch (Throwable e) {
            catchException = e;
        } finally {
            multiBuffer.restoredBuf(cacheData);
            multiBuffer.flip();
            notifyCallBack(multiBuffer, catchException);
        }

        if (catchException != null) {
            throw catchException;
        }
    }
}
