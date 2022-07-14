package com.jav.net.nio;


import com.jav.common.util.IoEnvoy;
import com.jav.net.base.AbsNetReceiver;
import com.jav.net.base.SocketChannelCloseException;
import com.jav.net.component.ByteBufferComponent;
import com.jav.net.component.joggle.IBufferComponent;
import com.jav.net.entity.MultiByteBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioReceiver extends AbsNetReceiver<SocketChannel, MultiByteBuffer> {

    protected final IBufferComponent mBufferComponent;

    public NioReceiver() {
        mBufferComponent = new ByteBufferComponent();
    }

    public NioReceiver(IBufferComponent component) {
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
        Throwable exception = null;
        MultiByteBuffer arrayBuf = mBufferComponent.useBuffer();
        ByteBuffer[] buffer = arrayBuf.getAllBuf();
        if (buffer == null) {
            throw new RuntimeException("## buf is busy !!!");
        }
        long code = IoEnvoy.FAIL;
        try {
            do {
                code = channel.read(buffer);
                if (arrayBuf.isFull()) {
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
