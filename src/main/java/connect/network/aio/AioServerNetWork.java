package connect.network.aio;

import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.ISSLFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioServerNetWork<T extends AioServerTask> extends BaseNetWork<T> implements CompletionHandler<Void, T> {

    private ISSLFactory mSSLFactory;
    private AbsNetFactory mNetFactory;

    protected AioServerNetWork(ISSLFactory sslFactory, AbsNetFactory netFactory) {
        this.mSSLFactory = sslFactory;
        this.mNetFactory = netFactory;
    }

    @Override
    protected void onConnectTask(T task) {
        try {
            AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open(task.getChannelGroup());
            InetSocketAddress hostAddress = new InetSocketAddress(task.getHost(), task.getPort());
            channel.bind(hostAddress);
            task.setServerChannel(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void result, T attachment) {

    }

    @Override
    public void failed(Throwable exc, T attachment) {

    }
}
