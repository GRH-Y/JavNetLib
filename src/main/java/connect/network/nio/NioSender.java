package connect.network.nio;


import connect.network.base.joggle.INetSender;
import util.IoEnvoy;

import java.nio.channels.SocketChannel;

public class NioSender implements INetSender {

    private SocketChannel channel;

    public NioSender() {
    }

    public NioSender(SocketChannel channel) {
        this.channel = channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * 发送数据
     *
     * @param data
     */
    @Override
    public void sendData(byte[] data) {
        sendDataImp(data);
    }

    protected void sendDataImp(byte[] data) {
        try {
            IoEnvoy.writeToFull(channel, data);
        } catch (Throwable e) {
            e.printStackTrace();
            onSenderErrorCallBack(e);
        }
    }

    /**
     * 发送数据失败回调
     */
    protected void onSenderErrorCallBack(Throwable e) {
    }

}
