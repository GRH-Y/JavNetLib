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
                //如果是TLS初始化SSLEngine
                initSSLConnect(task);
            } else {
                if (!channel.isOpen() || channel.isRegistered()) {
                    throw new IllegalStateException("channel is unavailable !!! ");
                }
                if (channel.isBlocking()) {
                    // 设置为非阻塞
                    channel.configureBlocking(false);
                }
            }
            if (channel.isConnected()) {
                if (task.isTLS()) {
                    task.onHandshake(task.getSslEngine(), task.getSocketChannel());
                }
                task.onConnectCompleteChannel(task.getSocketChannel());
                SelectionKey selectionKey = task.getSocketChannel().register(mSelector, SelectionKey.OP_READ, task);
                task.setSelectionKey(selectionKey);
            } else {
                SelectionKey selectionKey = task.getSocketChannel().register(mSelector, SelectionKey.OP_CONNECT, task);
                task.setSelectionKey(selectionKey);
            }
        } catch (Throwable e) {
            LogDog.e("## url = " + task.getHost() + " port = " + task.getPort());
            e.printStackTrace();
            //该通道有异常，结束任务
            addDestroyTask(task);
        }
    }

//    /**
//     * 通知连接结果
//     *
//     * @param isConnect    是否连接成功
//     * @param isRegConnect 是否注册过连接事件
//     * @param task         任务
//     * @throws IOException
//     */
//    private void notifyConnect(boolean isConnect, boolean isRegConnect, T task) throws Throwable {
//        if ((isRegConnect && isConnect) || !isRegConnect) {
//            //注册过连接则不需要再注册，连接成功则注册读事件
//            int ops = isConnect ? SelectionKey.OP_READ : SelectionKey.OP_CONNECT;
//            SelectionKey selectionKey = task.getSocketChannel().register(mSelector, ops, task);
//            task.setSelectionKey(selectionKey);
//        }
//        if (!isConnect && !isRegConnect) {
//            //当前状态是没有注册过连接事件而且连接失败（等待连接事件回调再处理）
//            return;
//        }
//        if (isConnect) {
//            if (task.isTLS()) {
//                task.onHandshake(task.getSslEngine(), task.getSocketChannel());
//            }
//            task.onConnectCompleteChannel(task.getSocketChannel());
//        }
//        if (isRegConnect && !isConnect) {
//            //连接失败回调
//            task.onConnectError();
//            //连接失败则结束任务
//            addDestroyTask(task);
//        }
//    }


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
        channel.connect(address);
        task.setChannel(channel);
        return channel;
    }

    private void initSSLConnect(T task) {
        if (task.isTLS()) {
            if (mSslFactory != null) {
                SSLContext sslContext = mSslFactory.getSSLContext();
                SSLEngine sslEngine = sslContext.createSSLEngine(task.getHost(), task.getPort());
                sslEngine.setUseClientMode(true);
                sslEngine.setEnableSessionCreation(true);
                task.setSslEngine(sslEngine);
//                sslEngine.beginHandshake();
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
        if (isCanConnect) {
            boolean isConnect = false;
            try {
                isConnect = channel.finishConnect();
            } catch (Throwable e) {
                LogDog.e("## url = " + task.getHost() + " port = " + task.getPort());
                e.printStackTrace();
            }
            Throwable throwable = null;
            if (isConnect) {
                try {
                    if (task.isTLS()) {
                        task.onHandshake(task.getSslEngine(), task.getSocketChannel());
                    }
                    task.onConnectCompleteChannel(task.getSocketChannel());
                    selectionKey = task.getSocketChannel().register(mSelector, SelectionKey.OP_READ, task);
                    task.setSelectionKey(selectionKey);
                } catch (Throwable e) {
                    throwable = e;
                    e.printStackTrace();
                }
            }
            if (!isConnect || throwable != null) {
                //连接失败回调
                try {
                    task.onConnectError();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                //连接失败则结束任务
                addDestroyTask(task);
            }
        } else if (isCanRead) {
            NioReceiver receive = task.getReceive();
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
            NioReceiver receiver = task.getReceive();
            if (receiver != null) {
                receiver.onRelease();
            }
            task.setChannel(null);
        }
    }

    @Override
    public void onRecoveryTask(T task) {
        super.onRecoveryTask(task);
        task.setReceive(null);
        task.setSender(null);
    }
}
