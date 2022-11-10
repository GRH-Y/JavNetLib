package com.jav.net.nio;


import com.jav.net.base.AbsNetReceiver;
import com.jav.net.base.SocketChannelCloseException;
import com.jav.net.component.DefaultByteBufferComponent;
import com.jav.net.component.joggle.IBufferComponent;
import com.jav.net.entity.MultiByteBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioReceiver extends AbsNetReceiver<SocketChannel, MultiByteBuffer> {

    protected final IBufferComponent<MultiByteBuffer> mBufferComponent;

    public NioReceiver() {
        mBufferComponent = new DefaultByteBufferComponent();
    }

    public NioReceiver(IBufferComponent<MultiByteBuffer> component) {
        if (component == null) {
            throw new NullPointerException("BufferComponent can be not null !!!");
        }
        mBufferComponent = component;
    }

    public IBufferComponent getBufferComponent() {
        return mBufferComponent;
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    @Override
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        Throwable catchException = null;
        MultiByteBuffer arrayBuf = mBufferComponent.useBuffer();

        try {
            onReadImp(channel, arrayBuf);
        } catch (Throwable e) {
            catchException = e;
        } finally {
            notifyReceiverImp(arrayBuf, catchException);
        }

        if (catchException != null) {
            throw catchException;
        }
    }

    protected void onReadImp(SocketChannel channel, MultiByteBuffer arrayBuf) throws Throwable {
        long code;
        ByteBuffer[] buffer = arrayBuf.getAllBuf();
        if (buffer == null) {
            throw new RuntimeException("## buf is busy !!!");
        }
        try {
            do {
                code = channel.read(buffer);
                if (arrayBuf.isFull()) {
                    arrayBuf.setBackBuf(buffer);
                    buffer = arrayBuf.getAllBuf();
                }
            } while (code > 0);
        } catch (Throwable e) {
            throw e;
        } finally {
            arrayBuf.setBackBuf(buffer);
            arrayBuf.flip();
        }
        if (code < 0) {
            throw new SocketChannelCloseException();
        }
    }


    protected void notifyReceiverImp(MultiByteBuffer buf, Throwable exception) {
        if (mReceiverCallBack != null) {
            boolean ret = mReceiverCallBack.onReceiveFullData(buf, exception);
            if (ret) {
                mBufferComponent.reuseBuffer(buf);
            }
        }
    }
}
