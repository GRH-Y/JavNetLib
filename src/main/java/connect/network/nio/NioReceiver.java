package connect.network.nio;


import connect.network.base.joggle.INetReceiver;
import connect.network.nio.buf.MultilevelBuf;

import java.io.IOException;
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
                if (ret > 0 && ret == buffer.position()) {
                    buf.setBackBuf(buffer);
                    buffer = buf.getLendBuf();
                }
            } while (ret > 0 && channel.isOpen());
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            buf.setBackBuf(buffer);
            try {
                onInterceptReceive(buf, exception);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                buf.clear();
            }
            if (ret < 0) {
                throw new IOException("SocketChannel close !!!");
            }
        }
    }

    protected void notifyReceiver(T data, Exception exception) {
        if (receiver != null) {
            receiver.onReceive(data, exception);
        }
    }

    protected void onInterceptReceive(MultilevelBuf buf, Exception e) throws Exception {
    }

    protected void onRelease() {
        if (buf != null) {
            buf.release();
        }
    }
}
