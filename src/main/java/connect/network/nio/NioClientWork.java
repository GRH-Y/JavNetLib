package connect.network.nio;


import connect.network.base.joggle.ISSLFactory;
import log.LogDog;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioClientWork<T extends NioClientTask> extends NioNetWork<T> {


    private NioClientFactory mFactory;

    protected NioClientWork(NioClientFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("NioClientWork factory can not be null !");
        }
        this.mFactory = factory;
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
                //如果是TLS初始化SSLEngine
                initSSLConnect(task);
            } else {
                if (!channel.isOpen() && channel.isRegistered()) {
                    throw new IllegalStateException("channel is unavailable !!! ");
                }
                if (channel.isBlocking()) {
                    // 设置为非阻塞
                    channel.configureBlocking(false);
                }
            }
            notifyConnect(channel.isConnected(), false, task);
        } catch (Throwable e) {
            LogDog.e("url = " + task.getHost() + " port = " + task.getPort());
            e.printStackTrace();
            mFactory.removeTaskInside(task, false);
        }
    }

    /**
     * 通知连接结果
     *
     * @param isConnect 是否连接成功
     * @param isReg     是否注册过
     * @param task      任务
     * @throws IOException
     */
    private void notifyConnect(boolean isConnect, boolean isReg, T task) throws Throwable {
        if ((isReg && isConnect) || !isReg) {
            //注册过连接则不需要再注册，连接成功则注册读事件
            int ops = isConnect ? SelectionKey.OP_READ : SelectionKey.OP_CONNECT;
            SelectionKey selectionKey = task.getSocketChannel().register(mSelector, ops, task);
            task.setSelectionKey(selectionKey);
        }
        if (!isConnect && !isReg) {
            //在没有注册过的情况下而且连接失败
            return;
        }
        if (task.isTLS()) {
            task.onHandshake(task.getSslEngine(), task.getSocketChannel());
        }
        try {
            task.onConnectCompleteChannel(isConnect, task.getSocketChannel(), task.getSslEngine());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (isReg && !isConnect) {
            //连接失败则移除任务
            mFactory.removeTaskInside(task, false);
        }
    }

    /**
     * 创建通道
     *
     * @param task
     * @return
     */
    private SocketChannel createSocketChannel(T task) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(task.getHost(), task.getPort()));
        task.setChannel(channel);
        return channel;
    }

    private SSLEngine initSSLConnect(T task) throws IOException {
        SSLEngine sslEngine = null;
        if (task.isTLS()) {
            ISSLFactory sslFactory = mFactory.getSslFactory();
            if (sslFactory != null) {
                SSLContext sslContext = sslFactory.getSSLContext();
                sslEngine = sslContext.createSSLEngine(task.getHost(), task.getPort());
                sslEngine.setUseClientMode(true);
                sslEngine.setEnableSessionCreation(true);
                sslEngine.beginHandshake();
                task.setSslEngine(sslEngine);
            }
        }
        return sslEngine;
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
//        if (task.isTaskNeedClose()) {
//            return;
//        }
        boolean isCanConnect = selectionKey.isValid() && selectionKey.isConnectable();
        boolean isCanRead = selectionKey.isValid() && selectionKey.isReadable();
        if (isCanConnect) {
            boolean isConnect = false;
            try {
                isConnect = channel.finishConnect();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    notifyConnect(isConnect, true, task);
                } catch (Throwable e) {
                    e.printStackTrace();
                    mFactory.removeTaskInside(task, false);
                }
            }
        } else if (isCanRead) {
            NioReceive receive = task.getReceive();
            if (receive != null) {
                try {
                    receive.onRead(channel);
                } catch (Throwable e) {
                    mFactory.removeTaskInside(task, false);
                    LogDog.e("==> receive data has error = " + e.getMessage());
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
            task.setChannel(null);
            SSLEngine sslEngine = task.getSslEngine();
            if (sslEngine != null) {
                try {
                    SSLSession handSession = sslEngine.getHandshakeSession();
                    if (handSession != null) {
                        handSession.invalidate();
                    }
                    SSLSession sslSession = sslEngine.getSession();
                    if (sslSession != null) {
                        sslSession.invalidate();
                    }
                    sslEngine.closeOutbound();
//                    sslEngine.closeInbound();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
