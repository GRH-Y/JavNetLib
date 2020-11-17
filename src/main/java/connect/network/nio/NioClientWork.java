package connect.network.nio;


import connect.network.base.SocketChannelCloseException;
import connect.network.base.joggle.ISSLFactory;
import connect.network.ssl.TLSHandler;
import log.LogDog;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioClientWork<T extends NioClientTask> extends NioNetWork<T> {

    private ISSLFactory mSslFactory;

    protected NioClientWork(ISSLFactory factory) {
        this.mSslFactory = factory;
    }


    //---------------------------------------------------------------------------------------------------------------

    /**
     * 准备链接
     *
     * @param task
     */
    @Override
    public void onConnectTask(T task) {
        SocketChannel channel = task.getSocketChannel();
        try {
            if (channel == null) {
                //创建通道
                channel = createSocketChannel(task);
                if (channel.isConnected()) {
                    //如果是TLS初始化SSLEngine
                    initSSLConnect(task, channel);
                }
            } else {
                if (!channel.isOpen() || channel.isRegistered()) {
                    throw new IllegalStateException("channel is unavailable !!! ");
                }
                if (channel.isBlocking()) {
                    // 设置为非阻塞
                    channel.configureBlocking(false);
                }
                TLSHandler tlsHandler = task.getTlsHandler();
                if (tlsHandler != null) {
                    SSLEngine sslEngine = tlsHandler.getSslEngine();
                    sslEngine.beginHandshake();
                    tlsHandler.doHandshake(channel);
                }
            }
            if (channel.isConnected()) {
                SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_READ, task);
                task.setSelectionKey(selectionKey);
                task.onConnectCompleteChannel(channel);
            } else {
                SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_CONNECT, task);
                task.setSelectionKey(selectionKey);
            }
        } catch (Throwable e) {
            LogDog.e("## url = " + task.getHost() + " port = " + task.getPort());
            e.printStackTrace();
            //该通道有异常，结束任务
            addDestroyTask(task);
        }
    }


    /**
     * 创建通道
     *
     * @param task
     * @return
     */
    private SocketChannel createSocketChannel(T task) throws IOException {
        InetSocketAddress address = new InetSocketAddress(task.getHost(), task.getPort());
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().setKeepAlive(true);
        channel.socket().setReuseAddress(true);
        channel.socket().setTcpNoDelay(false);
        channel.socket().setOOBInline(false);
        channel.socket().setPerformancePreferences(0, 1, 2);
        channel.connect(address);
        task.setChannel(channel);
        return channel;
    }

    private void initSSLConnect(T task, SocketChannel channel) throws Throwable {
        if (task.isTLS()) {
            SSLContext sslContext = mSslFactory.getSSLContext();
            SSLEngine sslEngine = sslContext.createSSLEngine(task.getHost(), task.getPort());
            sslEngine.setUseClientMode(true);
            sslEngine.setEnableSessionCreation(true);
            if (task.isTLS()) {
                task.onHandshake(sslEngine, channel);
            }
        }
    }


    /**
     * 处理通道事件
     *
     * @param selectionKey 选择子
     */
    @Override
    public void onSelectionKey(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        T task = (T) selectionKey.attachment();
        boolean isCanConnect = selectionKey.isValid() && selectionKey.isConnectable();
        boolean isCanRead = selectionKey.isValid() && selectionKey.isReadable();
        boolean isCanWrite = selectionKey.isValid() && selectionKey.isWritable();
        if (isCanConnect) {
            Throwable throwable = null;
            boolean isConnect = false;
            try {
                isConnect = channel.finishConnect();
            } catch (Throwable e) {
                LogDog.e("## url = " + task.getHost() + " port = " + task.getPort());
                e.printStackTrace();
                throwable = e;
            }
            if (isConnect) {
                try {
                    //如果是TLS初始化SSLEngine(只能在连接成功后执行)
                    initSSLConnect(task, channel);
                    selectionKey = task.getSocketChannel().register(mSelector, SelectionKey.OP_READ, task);
                    task.setSelectionKey(selectionKey);
                    task.onConnectCompleteChannel(task.getSocketChannel());
                } catch (Throwable e) {
                    throwable = e;
                    e.printStackTrace();
                }
            }
            if (!isConnect || throwable != null) {
                //连接失败回调
                try {
                    task.onConnectError(throwable);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                //连接失败则结束任务
                addDestroyTask(task);
            }
        } else if (isCanRead) {
            NioReceiver receive = task.getReceiver();
            if (receive != null) {
                try {
                    receive.onRead(channel);
                } catch (Throwable e) {
                    addDestroyTask(task);
                    if (!(e instanceof SocketChannelCloseException)) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (isCanWrite) {
            NioSender sender = task.getSender();
            if (sender != null) {
                try {
                    sender.doSendData();
                } catch (Throwable e) {
                    addDestroyTask(task);
                    if (!(e instanceof SocketChannelCloseException)) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 准备断开链接回调
     *
     * @param task 网络请求任务
     */
    @Override
    public void onDisconnectTask(T task) {
        try {
            task.onCloseClientChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            TLSHandler tlsHandler = task.getTlsHandler();
            if (tlsHandler != null) {
                tlsHandler.release();
            }
        }
    }

}
