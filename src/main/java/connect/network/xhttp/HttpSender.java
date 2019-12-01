package connect.network.xhttp;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioSender;

public class HttpSender extends NioSender {

    public HttpSender(NioClientTask clientTask) {
        super(clientTask);
    }

    @Override
    protected void onSenderErrorCallBack() {
        NioHPCClientFactory.getFactory().removeTask(clientTask);
    }
}
