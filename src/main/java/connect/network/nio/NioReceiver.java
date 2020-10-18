package connect.network.nio;


import connect.network.base.SocketChannelCloseException;
import connect.network.base.joggle.INetReceiver;
import connect.network.nio.buf.MultilevelBuf;
import connect.network.xhttp.XMultiplexCacheManger;
import log.LogDog;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioReceiver<T> {

    protected INetReceiver<T> receiver;

    public NioReceiver() {
    }

    public NioReceiver(INetReceiver<T> receiver) {
        this.receiver = receiver;
    }

    public void setDataReceiver(INetReceiver<T> receiver) {
        this.receiver = receiver;
    }


    public void resetMultilevelBuf(MultilevelBuf buf) {
        XMultiplexCacheManger.getInstance().lose(buf);
    }


    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onRead(SocketChannel channel) throws Exception {
        Exception exception = null;
        MultilevelBuf buf = XMultiplexCacheManger.getInstance().obtainBuf();
        ByteBuffer buffer = buf.getLendBuf();
        int ret = -1;
        try {
            do {
                ret = channel.read(buffer);
                if (ret > 0 && buffer.position() == buffer.capacity()) {
                    buf.setBackBuf(buffer);
                    buffer = buf.getLendBuf();
                }
            } while (ret > 0 && channel.isConnected());
        } catch (Exception e) {
            exception = e;
        }finally {
            buf.setBackBuf(buffer);
            buf.flip();
        }
        try {
            notifyReceiverImp(buf, exception);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (exception != null) {
            throw exception;
        }else if (ret < 0 && exception == null) {
            InetSocketAddress localAddress = (InetSocketAddress) channel.getLocalAddress();
            InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
            LogDog.e("## read data return - 1 ， local address = " + localAddress.toString() + " remote address = " + remoteAddress.toString());
            throw new SocketChannelCloseException();
        }
    }

    protected void notifyReceiverImp(MultilevelBuf buf, Exception e) {
        if (receiver != null) {
            receiver.onReceiveFullData((T) buf, e);
        }
    }
}
