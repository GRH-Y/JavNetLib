package com.jav.net.nio;


import com.jav.net.base.AbsNetReceiver;
import com.jav.net.base.SocketChannelCloseException;
import com.jav.net.entity.MultiByteBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * nio 数据接收者
 *
 * @author yyz
 */
public class NioReceiver extends AbsNetReceiver<SocketChannel, MultiByteBuffer> {

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    @Override
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        Throwable catchException = null;
        MultiByteBuffer newBuffer = new MultiByteBuffer();

        try {
            onReadImp(channel, newBuffer);
        } catch (Throwable e) {
            catchException = e;
        } finally {
            notifyCallBack(newBuffer, catchException);
        }
        if (catchException != null) {
            throw catchException;
        }
    }

    protected void onReadImp(SocketChannel channel, MultiByteBuffer newBuffer) throws Throwable {
        long code;
        ByteBuffer[] buffer = newBuffer.getAllBuf();
        if (buffer == null) {
            throw new RuntimeException("## buf is busy !!!");
        }
        try {
            do {
                code = channel.read(buffer);
                if (newBuffer.isFull()) {
                    newBuffer.setBackBuf(buffer);
                    buffer = newBuffer.getAllBuf();
                }
            } while (code > 0);
        } finally {
            newBuffer.setBackBuf(buffer);
            newBuffer.flip();
        }
        if (code < 0) {
            throw new SocketChannelCloseException();
        }
    }

    protected void notifyCallBack(MultiByteBuffer buf, Throwable e) {
        if (mReceiver != null) {
            if (!buf.isClear()) {
                mReceiver.onReceiveFullData(buf);
            }
            if (e != null) {
                mReceiver.onReceiveError(e);
            }
        }
    }

}
