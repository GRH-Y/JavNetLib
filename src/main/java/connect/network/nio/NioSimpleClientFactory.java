package connect.network.nio;

import connect.network.base.AbstractNioFactory;
import connect.network.base.NioEngine;
import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.SSLSocketFactory;
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

    /**
     * 触发Selector监听OP_WRITE
     *
     * @param sender
     */
    protected void registerWrite(NioSender sender) {
        try {
            SocketChannel channel = sender.getChannel();
            SelectionKey selectionKey = channel.keyFor(mSelector);
            if (selectionKey != null) {
                NioClientTask task = (NioClientTask) selectionKey.attachment();
                channel.register(mSelector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, task);
                mSelector.wakeup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解除Selector监听OP_WRITE（不解除会导致selector永远得到写的事件，浪费性能）
     *
     * @param sender
     */
    protected void unRegisterWrite(NioSender sender) {
        try {
            SocketChannel channel = sender.getChannel();
            SelectionKey selectionKey = channel.keyFor(mSelector);
            if (selectionKey != null) {
                NioClientTask task = (NioClientTask) selectionKey.attachment();
                channel.register(mSelector, SelectionKey.OP_READ, task);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void registerChannel(NioClientTask task, SocketChannel channel) throws Exception {
        if (task.getReceive() != null && task.getSender() != null) {
            task.getSender().setChannel(channel);
            task.getReceive().setChannel(channel);
            channel.register(mSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, task);
        } else if (task.getSender() != null) {
            task.getSender().setChannel(channel);
            channel.register(mSelector, SelectionKey.OP_WRITE, task);
        } else if (task.getReceive() != null) {
            task.getReceive().setChannel(channel);
            channel.register(mSelector, SelectionKey.OP_READ, task);
        }
    }


    private SocketChannel createSocketChannel(NioClientTask task) {
        SocketChannel socketChannel = null;
        if (task.getHost() != null && task.getPort() > 0) {
            try {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                boolean result = socketChannel.connect(new InetSocketAddress(task.getHost(), task.getPort()));
                if (!result) {
                    long startTime = System.currentTimeMillis();
                    while (!socketChannel.finishConnect()) {
                        if (System.currentTimeMillis() - startTime < task.getConnectTimeout()) {
                            Thread.sleep(100);
                        } else {
                            throw new SocketTimeoutException("connect " + task.getHost() + ":" + task.getPort() + " timeout !!!");
                        }
                    }
                }
                task.setChannel(socketChannel);
                if (!task.isAutoCheckCertificate() && task.getPort() == 443 && mSslFactory != null) {
                    SSLSocketFactory sslSocketFactory = mSslFactory.getSSLSocketFactory();
                    SSLSocketImpl sslSocketImpl = (SSLSocketImpl) sslSocketFactory.createSocket(task.getHost(), task.getPort());
                    sslSocketImpl.setSoTimeout(task.getConnectTimeout());
                    sslSocketImpl.setUseClientMode(true);
                    sslSocketImpl.startHandshake();
                    task.setSSLSocket(sslSocketImpl);
                }
            } catch (Exception e) {
                socketChannel = null;
                e.printStackTrace();
            }
        }
        return socketChannel;
    }


    @Override
    protected void onConnectTask(NioClientTask task) {
        SocketChannel channel = task.getSocketChannel();
//        Logcat.d("==> NioClientFactory onConnectTask");
        if (channel == null) {
            channel = createSocketChannel(task);
        }
        //创建失败
        if (channel == null) {
            task.onConnectSocketChannel(false);
            removeTask(task);
            return;
        }
        try {
            Socket socket = channel.socket();
            socket.setSoTimeout(task.getConnectTimeout());
            //复用端口
            socket.setReuseAddress(true);
            socket.setKeepAlive(true);
            //关闭Nagle算法
            socket.setTcpNoDelay(true);
            //执行Socket的close方法，该方法也会立即返回
            socket.setSoLinger(true, 0);
//            configSSL(task);
            task.onConfigSocket(channel);

            if (channel.isBlocking()) {
                channel.configureBlocking(false);// 设置为非阻塞
            }
            if (channel.isConnected()) {
                registerChannel(task, channel);
                task.onConnectSocketChannel(true);
            } else {
                synchronized (AbstractNioFactory.class) {
                    channel.register(mSelector, SelectionKey.OP_CONNECT, task);
                }
            }
        } catch (Exception e) {
            task.onConnectSocketChannel(false);
            removeTask(task);
            e.printStackTrace();
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

        if (selectionKey.isValid() && selectionKey.isConnectable() && task != null) {
//                Logcat.d("==> selectionKey isConnectable ");
            boolean isOpen = channel.isConnected();
            try {
                if (channel.isConnectionPending()) {
                    isOpen = channel.finishConnect();// 完成连接
                }
            } catch (Exception e) {
                removeTask(task);
                e.printStackTrace();
            } finally {
                if (isOpen) {
                    try {
                        registerChannel(task, channel);
                    } catch (Throwable e) {
                        removeTask(task);
                        e.printStackTrace();
                    }
                }
                task.onConnectSocketChannel(isOpen);
            }
        } else if (selectionKey.isValid() && selectionKey.isReadable() && task != null) {
//                Logcat.d("==> selector isReadable keys = " + selector.keys().size());
            NioReceive receive = task.getReceive();
            if (receive != null) {
                try {
                    receive.onRead(task.getSocketChannel());
                } catch (Throwable e) {
                    removeTask(task);
                    e.printStackTrace();
                }
            }
        } else if (selectionKey.isValid() && selectionKey.isWritable() && task != null) {
//                    System.out.println("==> selectionKey isWritable ");
            NioSender sender = task.getSender();
            if (sender != null) {
                try {
                    sender.onWrite(task.getSocketChannel());
                } catch (Throwable e) {
                    removeTask(task);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDisconnectTask(NioClientTask task) {
        try {
            task.onCloseSocketChannel();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            task.setChannel(null);
        }
    }

    @Override
    protected void onRecoveryTask(NioClientTask task) {
        try {
            task.onRecovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
