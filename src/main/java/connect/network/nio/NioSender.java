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
        if (data != null && channel != null) {
            try {
                if (!clientTask.isTaskNeedClose()) {
                    int ret;
                    do {
                        ret = channel.write(ByteBuffer.wrap(data));
                    } while (ret == 0);
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
