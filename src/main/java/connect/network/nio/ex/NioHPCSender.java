package connect.network.nio.ex;


import connect.network.nio.NioSender;
import connect.network.nio.SimpleSendTask;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 高性能的发送者 (独立线程处理发送数据)
 */
public class NioHPCSender extends NioSender {

    public NioHPCSender(){
    }

    public NioHPCSender(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void setChannel(SocketChannel channel) {
        super.setChannel(channel);
        SimpleSendTask.getInstance().open();
    }

    @Override
    protected void sendDataImp(ByteBuffer data) {
        SimpleSendTask.getInstance().sendData(this, data);
    }
}
