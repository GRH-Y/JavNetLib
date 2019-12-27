package connect.network.nio;


import connect.network.base.joggle.INetReceive;
import util.IoEnvoy;
import util.ReflectionCall;

import java.nio.channels.SocketChannel;

public class NioReceive implements INetReceive {

    protected Object mReceive = null;
    protected String mReceiveMethod = null;

    protected SocketChannel channel = null;
    protected NioClientTask clientTask;

    public NioReceive(NioClientTask clientTask, Object receive, String receiveMethod) {
        this.clientTask = clientTask;
        this.mReceive = receive;
        this.mReceiveMethod = receiveMethod;
    }

    public NioReceive(NioClientTask clientTask) {
        this.clientTask = clientTask;
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
        this.mReceiveMethod = receiveMethodName;
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onRead() throws Exception {
        if (!clientTask.isTaskNeedClose()) {
            byte[] data = null;
            Exception exception = null;
            try {
                data = IoEnvoy.tryRead(channel);
            } catch (Exception e) {
                exception = e;
                throw new Exception(e);
            } finally {
                ReflectionCall.invoke(mReceive, mReceiveMethod, new Class[]{byte[].class, Exception.class}, new Object[]{data, exception});
            }
        }
    }

}
