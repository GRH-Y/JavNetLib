package connect.network.nio;


import connect.network.base.joggle.INetReceiver;
import util.IoEnvoy;

import java.nio.channels.SocketChannel;

public class NioReceiver<T> {

    protected INetReceiver<T> receiver;

    public NioReceiver() {
    }

    public NioReceiver(INetReceiver<T> receiver) {
        this.receiver = receiver;
    }


    public void setReceiver(INetReceiver<T> receiver) {
        this.receiver = receiver;
    }


    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onRead(SocketChannel channel) throws Exception {

        byte[] data = null;
        Exception exception = null;
        try {
            data = IoEnvoy.tryRead(channel);
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            onInterceptReceive(data, exception);
        }
    }

    protected void notifyReceiver(T data, Exception exception) {
        if (receiver != null) {
            receiver.onReceive(data, exception);
        }
    }

    protected void onInterceptReceive(byte[] data, Exception e) throws Exception {
    }
}
