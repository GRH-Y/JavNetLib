package connect.network.nio;


import connect.network.base.joggle.INetSender;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioSender implements INetSender {

    protected SocketChannel channel = null;

    protected NioClientTask clientTask;

    public NioSender(NioClientTask clientTask) {
        this.clientTask = clientTask;
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
        if (data != null && channel != null && !clientTask.isTaskNeedClose()) {
            try {
                int off = 0;
                int length = data.length;
                int len = length;
                ByteBuffer buffer = ByteBuffer.wrap(data);
                while (off < length) {
                    buffer.position(off);
                    buffer.limit(len);
                    off += channel.write(buffer);
                    len = length - off;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                onSenderErrorCallBack(e);
            }
        }
    }

    protected void setChannel(SocketChannel channel) {
        this.channel = channel;
    }


    public SocketChannel getChannel() {
        return channel;
    }

    /**
     * 发送数据失败回调
     */
    protected void onSenderErrorCallBack(Throwable e) {
    }

}
