package connect.network.aio;


import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.base.joggle.ISSLFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioNetWork<T extends AioClientTask> extends BaseNetWork<T> implements CompletionHandler<Void, T> {

    private ISSLFactory mSslFactory;
    private AbsNetFactory mNetFactory;

    protected AioNetWork(ISSLFactory sslFactory, AbsNetFactory netFactory) {
        this.mSslFactory = sslFactory;
        this.mNetFactory = netFactory;
    }

    @Override
    protected void onCheckConnectTask() {
        super.onCheckConnectTask();
    }

    @Override
    protected void onConnectTask(T task) {
        try {
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            task.setSocketChannel(client);
            task.setFactory(mNetFactory);
            client.connect(new InetSocketAddress(task.getHost(), task.getPort()), task, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSSLConnect(T task) throws Exception {
        if (task.isTLS()) {
            SSLContext sslContext = mSslFactory.getSSLContext();
            SSLEngine sslEngine = sslContext.createSSLEngine(task.getHost(), task.getPort());
            sslEngine.setUseClientMode(true);
            sslEngine.setEnableSessionCreation(true);
            if (task.isTLS()) {
                task.onHandshake(sslEngine, task.getSocketChannel());
            }
        }
    }

    @Override
    protected void onCheckRemoverTask() {
        super.onCheckRemoverTask();
    }

    @Override
    protected void onRecoveryTaskAll() {
        super.onRecoveryTaskAll();
    }

    @Override
    protected void onDisconnectTask(T task) {
        try {
            task.onCloseClientChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                task.getSocketChannel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            onRecoveryTask(task);
        }
    }

    @Override
    public void completed(Void result, T task) {
        try {
            initSSLConnect(task);
            task.onConnectCompleteChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, T attachment) {
        try {
            attachment.onConnectError(exc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
