package connect.network.nio;

import connect.network.base.AbstractNioFactory;
import connect.network.base.NioEngine;
import util.StringEnvoy;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * nio客户端工厂(单线程管理多个Socket)
 */
public class NioSimpleClientFactory extends AbstractNioFactory<NioClientTask> {

    protected NioSimpleClientFactory() {
        super();
    }

    protected NioSimpleClientFactory(NioEngine engine) {
        super(engine);
    }

//    /**
//     * 触发Selector监听OP_WRITE
//     *
//     * @param sender
//     */
//    protected void registerWrite(NioSender sender) {
//        try {
//            SocketChannel channel = sender.getChannel();
//            SelectionKey selectionKey = channel.keyFor(mSelector);
//            if (selectionKey != null && channel.isOpen()) {
//                NioClientTask task = (NioClientTask) selectionKey.attachment();
//                channel.register(mSelector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, task);
//                mSelector.wakeup();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 解除Selector监听OP_WRITE（不解除会导致selector永远得到写的事件，浪费性能）
//     *
//     * @param sender
//     */
//    protected void unRegisterWrite(NioSender sender) {
//        try {
//            SocketChannel channel = sender.getChannel();
//            SelectionKey selectionKey = channel.keyFor(mSelector);
//            if (selectionKey != null && channel.isOpen()) {
//                NioClientTask task = (NioClientTask) selectionKey.attachment();
//                channel.register(mSelector, SelectionKey.OP_READ, task);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * 注册读事件
     *
     * @param task
     * @param channel
     * @throws Exception
     */
    private void registerChannel(NioClientTask task, SocketChannel channel) throws Exception {
        if (task.getSender() != null) {
            task.getSender().setChannel(channel);
            task.getSender().setClientTask(task);
//            channel.register(mSelector, SelectionKey.OP_WRITE, task);
        }
        if (task.getReceive() != null) {
            task.getReceive().setChannel(channel);
            channel.register(mSelector, SelectionKey.OP_READ, task);
        }
    }


    private void createSocketChannel(NioClientTask task) throws Exception {
        if (StringEnvoy.isEmpty(task.getHost()) || task.getPort() < 0) {
            throw new IllegalArgumentException("## host or port is Illegal = " + task.getHost() + ":" + task.getPort());
        }
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(task.getHost(), task.getPort()));
        task.setChannel(socketChannel);
    }


    @Override
    protected void onConnectTask(NioClientTask task) {
        SocketChannel channel = task.getSocketChannel();
        try {
            if (channel == null) {
                createSocketChannel(task);
                channel = task.getSocketChannel();
            }
            configSocket(task, channel);
        } catch (Exception e) {
            task.onConnectSocketChannel(false);
            removeTask(task);
            e.printStackTrace();
        }
    }

    private void configSocket(NioClientTask task, SocketChannel channel) throws Exception {
        Socket socket = channel.socket();
        //复用端口
        socket.setReuseAddress(true);
//            socket.setKeepAlive(true);
        //设置超时时间
        socket.setSoTimeout(task.getConnectTimeout());
        //关闭Nagle算法
//        if (task.getConnectTimeout() > 0 && task.getPort() == 443) {
//            try {
//                socket.setTcpNoDelay(true);
//            } catch (Exception e) {
//                LogDog.e("The socket does not support setTcpNoDelay() !!! ");
////                e.printStackTrace();
//            }
//        }
        //执行Socket的close方法，该方法也会立即返回
        socket.setSoLinger(true, 0);
        if (channel.isBlocking()) {
            // 设置为非阻塞
            channel.configureBlocking(false);
        }
        //回调任务类处理最后的订制配置
        task.onConfigSocket(channel);
        if (channel.isConnected()) {
            registerChannel(task, channel);
            task.onConnectSocketChannel(true);
        } else {
            channel.register(mSelector, SelectionKey.OP_CONNECT, task);
        }
    }

//    private void configSSL(NioClientTask task) {
//        try {
//            if (task.getPort() == 443 && mSslFactory != null) {
//                SSLSocketFactory sslSocketFactory = mSslFactory.getSSLSocketFactory();
//                SSLSocketImpl sslSocketImpl = (SSLSocketImpl) sslSocketFactory.createSocket(task.getHost(), task.getPort());
//                sslSocketImpl.setSoTimeout(1000);
//                sslSocketImpl.setUseClientMode(true);
//                sslSocketImpl.startHandshake();
//                task.setSSLSocket(sslSocketImpl);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void onSelectionKey(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        NioClientTask task = (NioClientTask) selectionKey.attachment();

        if (selectionKey.isValid() && selectionKey.isConnectable()) {
            boolean isConnect = channel.isConnected();
            try {
                if (channel.isConnectionPending()) {
                    // 完成连接
                    isConnect = channel.finishConnect();
                    if (!isConnect && task.getConnectTimeout() > 0) {
                        long startTime = System.currentTimeMillis();
                        while (!channel.finishConnect()) {
                            if (System.currentTimeMillis() - startTime < task.getConnectTimeout()) {
                                Thread.sleep(100);
                            } else {
                                throw new SocketTimeoutException("connect " + task.getHost() + ":" + task.getPort() + " timeout !!!");
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                isConnect = false;
                e.printStackTrace();
            } finally {
                if (isConnect) {
                    try {
                        registerChannel(task, channel);
                    } catch (Throwable e) {
                        isConnect = false;
                        removeTask(task);
                        e.printStackTrace();
                    } finally {
                        task.onConnectSocketChannel(isConnect);
                    }
                } else {
                    task.onConnectSocketChannel(false);
                    removeTask(task);
                }
            }
        } else if (selectionKey.isValid() && selectionKey.isReadable()) {
            NioReceive receive = task.getReceive();
            if (receive != null) {
                try {
                    receive.onRead();
                } catch (Throwable e) {
                    removeTask(task);
                    e.printStackTrace();
                }
            }
        }
//        else if (selectionKey.isValid() && selectionKey.isWritable() && task != null) {
//            NioSender sender = task.getSender();
//            if (sender != null) {
//                try {
//                    sender.onWrite();
//                } catch (Throwable e) {
//                    removeTask(task);
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    @Override
    protected void onDisconnectTask(NioClientTask task) {
        try {
            task.onCloseSocketChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            task.setChannel(null);
        }
    }

    @Override
    protected void onRecoveryTask(NioClientTask task) {
        try {
            task.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
