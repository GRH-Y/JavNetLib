package connect.network.nio;


import connect.network.base.joggle.ISender;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioSender implements ISender {

    protected SocketChannel channel;
    protected NioClientTask clientTask;

    /**
     * 发送数据
     * @param data
     */
    @Override
    public void sendData(byte[] data) {
        sendDataNow(data);
    }

    /**
     * 发送数据
     * @param data
     */
    @Override
    public void sendDataNow(byte[] data) {
        boolean isError = true;
        if (data != null && channel != null && channel.isOpen()) {
            try {
                if (channel.isOpen() && channel.isConnected() && !clientTask.isCloseing()) {
                    isError = channel.write(ByteBuffer.wrap(data)) < 0;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (isError) {
            onSenderErrorCallBack();
        }
    }

    protected void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    protected void setClientTask(NioClientTask clientTask) {
        this.clientTask = clientTask;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * 发送数据失败回调
     */
    protected void onSenderErrorCallBack() {
    }

}
