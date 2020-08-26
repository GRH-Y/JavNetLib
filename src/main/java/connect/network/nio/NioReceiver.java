package connect.network.nio;


import connect.network.base.SocketChannelCloseException;
import connect.network.base.joggle.INetReceiver;
import connect.network.nio.buf.MultilevelBuf;
import log.LogDog;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioReceiver<T> {

    protected INetReceiver<T> receiver;
    protected MultilevelBuf buf;

    public NioReceiver() {
        init();
    }

    public NioReceiver(INetReceiver<T> receiver) {
        this.receiver = receiver;
        init();
    }

    public void setReceiver(INetReceiver<T> receiver) {
        this.receiver = receiver;
    }

    protected void init() {
        buf = new MultilevelBuf();
    }

    public void reset() {
        if (buf != null) {
            buf.clear();
        }
    }


    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onRead(SocketChannel channel) throws Exception {
        Exception exception = null;
        ByteBuffer buffer = buf.getLendBuf();
        int ret = -1;
        try {
            do {
                ret = channel.read(buffer);
                if (ret > 0 && buffer.position() == buffer.capacity()) {
                    buf.setBackBuf(buffer);
                    buffer = buf.getLendBuf();
                }
            } while (ret > 0 && channel.isOpen());
        } catch (Exception e) {
            InetSocketAddress localAddress = (InetSocketAddress) channel.getLocalAddress();
            InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
            LogDog.e("## read data has exception !!! " + e.getMessage());
            LogDog.e("## local address =  " + localAddress.toString());
            LogDog.e("## remote address =  " + remoteAddress.toString());
            exception = e;
            throw e;
        } finally {
            buf.setBackBuf(buffer);
            buf.flip();
            try {
                onReceiveFullData(buf);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                buf.clear();
            }
            if (exception != null) {
                onReceiveException(exception);
            }
            if (ret < 0 && exception == null) {
                LogDog.e("read data return - 1");
                throw new SocketChannelCloseException();
            }
        }
    }

    protected void onReceiveFullData(MultilevelBuf buf) {
        T result = null;
        try {
            result = (T) buf;
        } catch (Throwable e1) {
            try {
                result = (T) buf.array();
            } catch (Throwable e2) {
                LogDog.e("## need rewrite onInterceptReceive method !!!");
            }
        } finally {
            notifyReceiver(result);
        }
    }

    protected void notifyReceiver(T data) {
        if (receiver != null) {
            receiver.onReceiveFullData(data);
        }
    }

    protected void onReceiveException(Exception e) {
        if (receiver != null && e != null) {
            receiver.onReceiveException(e);
        }
    }

    protected void onRelease() {
        if (buf != null) {
            buf.release();
        }
    }
}
