package com.currency.net.nio;


import com.currency.net.base.AbsNetReceiver;
import com.currency.net.base.SocketChannelCloseException;
import com.currency.net.entity.MultiByteBuffer;
import util.IoEnvoy;
import util.MultiplexCache;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioReceiver extends AbsNetReceiver<SocketChannel, MultiByteBuffer> {

    protected final MultiplexCache<MultiByteBuffer> mBufCache;

    protected final static int MORE_BUF = 2;

    public NioReceiver() {
        mBufCache = new MultiplexCache();
    }

    public void reuseBuf(MultiByteBuffer buf) {
        if (!buf.isIdle()) {
            throw new RuntimeException("## buf is busy, can not be reset !!!");
        }
        mBufCache.resetData(buf);
    }

    protected MultiByteBuffer getBuf() {
        MultiByteBuffer buffer = mBufCache.getCanUseData();
        if (buffer == null) {
            buffer = new MultiByteBuffer();
        }
        return buffer;
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    @Override
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        Throwable exception = null;
        MultiByteBuffer arrayBuf = getBuf();
        ByteBuffer[] buffer = arrayBuf.getAllBuf();
        if (buffer == null) {
            throw new RuntimeException("## buf is busy !!!");
        }
        long code = IoEnvoy.FAIL;
        try {
            do {
                code = channel.read(buffer);
                if (code == MORE_BUF || arrayBuf.isFull()) {
                    arrayBuf.setBackBuf(buffer);
                    buffer = arrayBuf.getAllBuf();
                }
            } while (code > 0);
        } catch (Throwable e) {
            exception = e;
        } finally {
            arrayBuf.setBackBuf(buffer);
            arrayBuf.flip();
        }
        notifyReceiverImp(code, arrayBuf, exception);
    }


    protected void notifyReceiverImp(long code, MultiByteBuffer buf, Throwable exception) throws Throwable {
        if (mReceiverCallBack != null) {
            mReceiverCallBack.onReceiveFullData(buf, exception);
        }
        if (exception != null) {
            throw exception;
        } else if (code < 0) {
            throw new SocketChannelCloseException();
        }
    }
}
