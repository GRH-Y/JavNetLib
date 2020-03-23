package connect.network.nio;

import connect.network.base.joggle.ISSLFactory;
import log.LogDog;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioServerWork<T extends NioServerTask> extends NioNetWork<T> {


    private NioServerFactory mFactory;

    protected NioServerWork(NioServerFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("NioServerWork factory can not be null !");
        }
        this.mFactory = factory;
    }

    @Override
    public void onConnectTask(T task) {
        //创建服务，并注册到selector，监听所有的事件
        ServerSocketChannel channel = task.getServerSocketChannel();
        try {
            if (channel == null) {
                channel = createChannel(task);
            }
            if (channel != null) {
                //配置通道
//                configChannel(task, channel);
                try {
                    task.onBootServerComplete(!task.isTaskNeedClose(), task.isTaskNeedClose() ? null : channel);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                //注册通道
                registerChannel(task, channel);
            }
        } catch (Throwable e) {
            LogDog.e("url = " + task.getServerHost() + " port = " + task.getServerHost());
            e.printStackTrace();
            mFactory.removeTaskInside(task, false);
        }
    }

    private ServerSocketChannel createChannel(T task) throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(task.getServerHost(), task.getServerPort()), task.getMaxConnect());
        task.setServerSocketChannel(channel);
        return channel;
    }

    private void initSSLConnect(T task) throws SSLException {
        ISSLFactory sslFactory = mFactory.getSslFactory();
        if (sslFactory != null) {
            SSLContext sslContext = sslFactory.getSSLContext();
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setEnableSessionCreation(true);
            try {
                task.onConfigSSLEngine(sslEngine);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            sslEngine.beginHandshake();
            task.setSslEngine(sslEngine);
        }
    }

//    private void configChannel(T task, ServerSocketChannel channel) throws SocketException {
//        task.setServerSocketChannel(channel);
//        channel.socket().setSoTimeout(task.getAcceptTimeout());
//    }

    private void registerChannel(T task, ServerSocketChannel channel) throws ClosedChannelException {
        if (channel.isOpen()) {
            //注册监听链接事件
            SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_ACCEPT, task);
            task.setSelectionKey(selectionKey);
        } else {
            mFactory.removeTaskInside(task, false);
        }
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
                    initSSLConnect(task);
                }
                task.onAcceptServerChannel(channel);
            } catch (Throwable e) {
                e.printStackTrace();
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
