package connect.network.xhttp;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioSender;

public class XHttpSender extends NioSender {

    public XHttpSender(NioClientTask clientTask) {
        super(clientTask);
    }

    @Override
    protected void onSenderErrorCallBack() {
        NioHPCClientFactory.getFactory().removeTask(clientTask);
    }
}
