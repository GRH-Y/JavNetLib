package connect.network.nio;


import connect.network.base.joggle.INetReceive;
import util.IoEnvoy;

import java.nio.channels.SocketChannel;

public class NioReceive<T> {

    protected INetReceive<T> receive;

    public NioReceive(INetReceive<T> receive) {
        this.receive = receive;
    }

    protected void notifyReceiver(T data, Exception exception) {
        if (receive != null) {
            receive.onReceive(data, exception);
        }
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
            throw exception;
        } finally {
            if (data != null) {
                onResponse((T) data);
            }
            notifyReceiver((T) data, exception);
        }
    }

    protected void onResponse(T response) throws Exception {

    }
}
