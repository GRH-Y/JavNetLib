package com.currency.net.nio;


import com.currency.net.base.AbsNetReceiver;
import com.currency.net.base.SocketChannelCloseException;
import com.currency.net.xhttp.XMultiplexCacheManger;
import com.currency.net.xhttp.utils.ReuseDirectBuf;
import util.IoEnvoy;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioReceiver extends AbsNetReceiver<SocketChannel, ReuseDirectBuf> {

    public NioReceiver() {
    }

    public void reuseBuf(ReuseDirectBuf buf) {
        XMultiplexCacheManger.getInstance().lose(buf);
    }

    protected ReuseDirectBuf createBuf() {
        return XMultiplexCacheManger.getInstance().obtainBuf();
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    @Override
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        Throwable exception = null;
        ReuseDirectBuf buf = createBuf();
        ByteBuffer[] buffer = buf.getAllBuf();
        while (buffer == null) {
            buf = createBuf();
            buffer = buf.getAllBuf();
        }
        long ret = IoEnvoy.FAIL;
        try {
            do {
                ret = channel.read(buffer);
                if (ret > 0) {
                    buf.setBackBuf(buffer);
                    buffer = buf.getAllBuf();
                }
            } while (ret > 0);
        } catch (Throwable e) {
            exception = e;
        } finally {
            buf.setBackBuf(buffer);
            buf.flip();
        }
        notifyReceiverImp(buf, exception, ret);
    }

    protected void notifyReceiverImp(ReuseDirectBuf buf, Throwable exception, long ret) throws Throwable {
        try {
            if (mReceiverCallBack != null) {
                mReceiverCallBack.onReceiveFullData(buf, exception);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (exception != null) {
            throw exception;
        } else if (ret < 0) {
            throw new SocketChannelCloseException();
        }

    }
}
