package connect.network.nio;


import connect.network.base.joggle.IReceive;
import util.IoUtils;
import util.ThreadAnnotation;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class NioReceive implements IReceive {

    protected Object mReceive;
    protected String mReceiveMethodName;

    public NioReceive() {
    }

    public NioReceive(Object receive, String receiveMethodName) {
        this.mReceive = receive;
        this.mReceiveMethodName = receiveMethodName;
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
    protected boolean onRead(SocketChannel channel) throws IOException {
        byte[] data = IoUtils.tryRead(channel);
        if (data != null) {
            ThreadAnnotation.disposeMessage(mReceiveMethodName, mReceive, data);
        }
        return true;
    }
}
