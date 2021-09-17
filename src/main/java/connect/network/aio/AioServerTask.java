package connect.network.aio;

import connect.network.base.BaseTLSTask;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;

public class AioServerTask extends BaseTLSTask {

    private AsynchronousServerSocketChannel mServerChannel;

    protected void setServerChannel(AsynchronousServerSocketChannel channel) {
        this.mServerChannel = channel;
    }

    public AsynchronousChannelGroup getChannelGroup() {
        return null;
    }

}
