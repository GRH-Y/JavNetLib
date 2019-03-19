package connect.network.nio;


import connect.network.base.joggle.IReceive;
import util.IoUtils;
import util.ThreadAnnotation;

import java.nio.channels.SocketChannel;

public class NioReceive implements IReceive {

    protected Object mReceive;
    protected String mReceiveMethodName;

    protected SocketChannel channel = null;

    public NioReceive() {
    }

    public NioReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }


    protected void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }


    @Override
    public void setReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onRead(SocketChannel channel) throws Exception {
        byte[] data = IoUtils.tryRead(channel);
        if (data != null) {
            ThreadAnnotation.disposeMessage(mReceiveMethodName, mReceive, data);
        }
    }
}
