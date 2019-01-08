package connect.network.nio;


import sun.security.ssl.SSLSocketImpl;

import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * nio客户端工厂(单线程管理多个Socket)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientFactory extends AbstractNioFactory<NioClientTask> {

    private static NioClientFactory mFactory = null;


    public static synchronized NioClientFactory getFactory() {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioClientFactory();
                }
            }
        }
        return mFactory;
    }

    public static void destroy() {
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }

    private NioClientFactory() {
        super();
    }

    /**
     * 触发Selector监听OP_WRITE
     *
     * @param sender
     */
    protected void registerWrite(NioSender sender) {
        try {
            synchronized (NioClientFactory.class) {
                SocketChannel channel = sender.getChannel();
                if (channel != null) {
                    NioClientTask task = (NioClientTask) channel.keyFor(mSelector).attachment();
                    int ops = channel.validOps();
                    channel.register(mSelector, SelectionKey.OP_WRITE | ops);
                    channel.keyFor(mSelector).attach(task);
                    mSelector.wakeup();
                }
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
            synchronized (NioClientFactory.class) {
                SocketChannel channel = sender.getChannel();
                if (channel != null) {
                    NioClientTask task = (NioClientTask) channel.keyFor(mSelector).attachment();
                    channel.register(mSelector, SelectionKey.OP_READ);
                    channel.keyFor(mSelector).attach(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void registerChannel(NioClientTask task, Selector selector, SocketChannel channel) throws Exception {
        if (task.getReceive() != null && task.getSender() != null) {
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            task.getSender().setChannel(channel);
        } else if (task.getSender() != null) {
            channel.register(selector, SelectionKey.OP_WRITE);
            task.getSender().setChannel(channel);
        } else if (task.getReceive() != null) {
            channel.register(selector, SelectionKey.OP_READ);
        }
    }


    private SocketChannel createSocketChannel(NioClientTask task) {
        SocketChannel channel = null;
        if (task.getHost() != null && task.getPort() > 0) {
            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false);// 设置为非阻塞
                Socket socket = channel.socket();
                //复用端口
                socket.setReuseAddress(true);
                socket.setKeepAlive(true);
                //关闭Nagle算法
                socket.setTcpNoDelay(true);
                //执行Socket的close方法，该方法也会立即返回
                socket.setSoLinger(true, 0);
                configSSL(task, channel);
                task.onConfigSocket(channel);
                channel.connect(new InetSocketAddress(task.getHost(), task.getPort()));
                task.setChannel(channel);
            } catch (Exception e) {
                channel = null;
                e.printStackTrace();
            }
        }
        return channel;
    }


    @Override
    protected void onConnectTask(Selector selector, NioClientTask task) {
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
            if (channel.isBlocking()) {
                channel.configureBlocking(false);// 设置为非阻塞
            }
            if (channel.isConnected()) {
                task.onConnectSocketChannel(true);
                registerChannel(task, selector, channel);
            } else {
                channel.register(selector, SelectionKey.OP_CONNECT);
            }
            channel.keyFor(selector).attach(task);
        } catch (Exception e) {
            task.onConnectSocketChannel(false);
            removeTask(task);
            e.printStackTrace();
        }
    }

    private void configSSL(NioClientTask task, SocketChannel channel) {
        try {
            if (task.getPort() == 443 && mSslFactory != null) {
//                while (!channel.finishConnect()) {
//                    // 完成连接
//                }
//                SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
//                SSLSocketImpl socket = (SSLSocketImpl) socketFactory.createSocket(channel.socket(), task.getHost(), task.getPort(), true);
//                socket.setUseClientMode(true);
//                socket.setEnableSessionCreation(false);
//                socket.setNeedClientAuth(false);
//                socket.setWantClientAuth(false);
//                channel.configureBlocking(false);
//                socket.startHandshake();
                SSLSocketFactory sslSocketFactory = mSslFactory.getSSLSocketFactory();
                SSLSocketImpl sslSocketImpl = (SSLSocketImpl) sslSocketFactory.createSocket(task.getHost(), task.getPort());
                sslSocketImpl.setSoTimeout(3000);
                sslSocketImpl.setUseClientMode(true);
                sslSocketImpl.startHandshake();
                sslSocketImpl.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSelectorTask(Selector selector) {

//        Logcat.d("==> NioClientFactory onSelectorTask");
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {

            SelectionKey selectionKey = iterator.next();
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
                    task.onConnectSocketChannel(isOpen);
                    if (isOpen) {
//                        configSSL(task, channel);
                        try {
                            registerChannel(task, selector, channel);
                            channel.keyFor(selector).attach(task);
                        } catch (Throwable e) {
                            removeTask(task);
                            e.printStackTrace();
                        }
                    }
                }
            } else if (selectionKey.isValid() && selectionKey.isReadable() && task != null) {
//                Logcat.d("==> selector isReadable keys = " + selector.keys().size());
                NioReceive receive = task.getReceive();
                if (receive != null) {
                    try {
                        receive.onRead(channel);
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
                        sender.onWrite(channel);
                    } catch (Throwable e) {
                        removeTask(task);
                        e.printStackTrace();
                    }
                }
            }

            if (task != null) {
                // 删除已处理过的事件
                iterator.remove();
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
