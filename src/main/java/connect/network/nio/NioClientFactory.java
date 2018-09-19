package connect.network.nio;


import java.net.InetSocketAddress;
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
        mFactory = null;
    }

    private NioClientFactory() {
        super();
    }

//    @Override
//    public void addTask(NioClientTask task) {
//        super.addTask(task);
//        Logcat.d("==> NioClientFactory addTask mConnectCache.size = " + mConnectCache.size());
//    }

    private void registerChannel(NioClientTask task, Selector selector, SocketChannel channel) throws Exception {
        if (task.getReceive() != null && task.getSender() != null) {
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } else if (task.getSender() != null) {
            channel.register(selector, SelectionKey.OP_WRITE);
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
            mDestroyCache.add(task);
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
            mDestroyCache.add(task);
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

            if (selectionKey.isValid() && selectionKey.isConnectable()) {
//                Logcat.d("==> selectionKey isConnectable ");
                boolean isOpen = channel.isConnected();
                try {
                    if (channel.isConnectionPending()) {
                        isOpen = channel.finishConnect();// 完成连接
                    }
                } catch (Exception e) {
                    mDestroyCache.add(task);
                    e.printStackTrace();
                } finally {
                    task.onConnectSocketChannel(isOpen);
                    if (isOpen) {
                        try {
                            registerChannel(task, selector, channel);
                            channel.keyFor(selector).attach(task);
                        } catch (Exception e) {
                            mDestroyCache.add(task);
                            e.printStackTrace();
                        }
                    }
                }
            } else if (selectionKey.isValid() && selectionKey.isReadable()) {
//                Logcat.d("==> selector isReadable keys = " + selector.keys().size());
                NioReceive receive = task.getReceive();
                if (receive != null) {
                    try {
                        boolean ret = receive.onRead(channel);
                        if (!ret) {
                            mDestroyCache.add(task);
                        }
                    } catch (Exception e) {
                        mDestroyCache.add(task);
                        e.printStackTrace();
                    }
                }
            } else if (selectionKey.isValid() && selectionKey.isWritable()) {
//                    System.out.println("==> selectionKey isWritable ");
                NioSender sender = task.getSender();
                if (sender != null) {
                    try {
                        boolean ret = sender.onWrite(channel);
                        if (!ret) {
                            mDestroyCache.add(task);
                        }
                    } catch (Exception e) {
                        mDestroyCache.add(task);
                        e.printStackTrace();
                    }
                }
            }
        }
        iterator.remove();// 删除已处理过的事件


    }


    @Override
    protected void onDisconnectTask(NioClientTask task) {
        task.setChannel(null);
        task.onCloseSocketChannel();
    }

}
