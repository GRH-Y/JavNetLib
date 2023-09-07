package com.jav.net.nio;


import com.jav.net.base.AbsNetReceiver;
import com.jav.net.base.MultiBuffer;
import com.jav.net.base.SocketChannelCloseException;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * nio 数据接收者
 *
 * @author yyz
 */
public class NioReceiver extends AbsNetReceiver<SocketChannel, MultiBuffer> {

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    @Override
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        onReadImp(channel);
    }

    protected void onReadImp(SocketChannel channel) throws Throwable {
        long code = 0;
        Throwable catchException = null;
        MultiBuffer multiBuffer = new MultiBuffer();
        ByteBuffer[] buffer = multiBuffer.rentAllBuf();
        if (buffer == null) {
            throw new RuntimeException("## buf is busy !!!");
        }
        try {
            do {
                code = channel.read(buffer);
                //buf已用满容量
                if (multiBuffer.isFull()) {
                    //归还buf
                    multiBuffer.restoredBuf(buffer);
                    //扩容
                    multiBuffer.appendBuffer();
                    //再租用buf
                    buffer = multiBuffer.rentAllBuf();
                }
            } while (code > 0);
        } catch (Throwable e) {
            catchException = e;
        } finally {
            multiBuffer.restoredBuf(buffer);
            multiBuffer.flip();
            notifyCallBack(multiBuffer, catchException);
        }
        if (catchException != null) {
            throw catchException;
        }
        if (code < 0) {
            throw new SocketChannelCloseException();
        }
    }

    protected void notifyCallBack(MultiBuffer buf, Throwable e) {
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
