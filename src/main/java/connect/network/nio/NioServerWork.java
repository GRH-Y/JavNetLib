package connect.network.nio;

import connect.network.base.joggle.ISSLFactory;
import log.LogDog;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioServerWork<T extends NioServerTask> extends NioNetWork<T> {

    private ISSLFactory mSslFactory;

    protected NioServerWork(ISSLFactory factory) {
        this.mSslFactory = factory;
    }

    @Override
    public void onConnectTask(T task) {
        //创建服务，并注册到selector，监听所有的事件
        ServerSocketChannel channel = task.getServerSocketChannel();
        try {
            if (channel == null) {
                channel = createChannel(task);
            } else {
                if (!channel.isOpen() && channel.isRegistered()) {
                    throw new IllegalStateException("channel is unavailable !!! ");
                }
                if (channel.isBlocking()) {
                    // 设置为非阻塞
                    channel.configureBlocking(false);
                }
            }
            try {
                task.onBootServerComplete(channel);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            //注册通道
            registerChannel(task, channel);
        } catch (Throwable e) {
            LogDog.e("url = " + task.getServerHost() + " port = " + task.getServerHost());
            e.printStackTrace();
            addDestroyTask(task);
        }
    }

    private ServerSocketChannel createChannel(T task) throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(task.getServerHost(), task.getServerPort()), task.getMaxConnect());
        task.setServerSocketChannel(channel);
        return channel;
    }

    private void initSSLConnect(T task, SocketChannel channel) throws Throwable {
        if (mSslFactory != null) {
            SSLContext sslContext = mSslFactory.getSSLContext();
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setEnableSessionCreation(true);
            task.onHandshake(sslEngine, channel);
        }
    }

    private void registerChannel(T task, ServerSocketChannel channel) throws ClosedChannelException {
        //注册监听链接事件
        SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_ACCEPT, task);
        task.setSelectionKey(selectionKey);
    }

    @Override
    public void onSelectionKey(SelectionKey selectionKey) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        T task = (T) selectionKey.attachment();
        if (selectionKey.isValid() && selectionKey.isAcceptable()) {
            try {
                SocketChannel channel = serverSocketChannel.accept();
                if (task.isTLS()) {
                    //如果是TLS初始化SSLEngine
                    initSSLConnect(task, channel);
                }
                task.onAcceptServerChannel(channel);
            } catch (Throwable e) {
                e.printStackTrace();
                addDestroyTask(task);
            }
        }
    }

    @Override
    public void onDisconnectTask(T task) {
        try {
            task.onCloseServerChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            task.setServerSocketChannel(null);
        }
    }

}
