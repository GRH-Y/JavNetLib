package connect.network.xhttp;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioSender;

import java.nio.channels.SocketChannel;

public class XHttpSender extends NioSender {

    private NioClientTask clientTask;

    public XHttpSender(NioClientTask clientTask, SocketChannel channel) {
        super(channel);
        this.clientTask = clientTask;
    }

    @Override
    protected void onSenderErrorCallBack(Throwable e) {
        NioHPCClientFactory.getFactory().removeTask(clientTask);
    }
}
