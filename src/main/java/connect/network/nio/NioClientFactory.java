package connect.network.nio;


import task.utils.Logcat;

import java.io.IOException;
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


    public static NioClientFactory getFactory() throws IOException {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioClientFactory();
                }
            }
        }
        return mFactory;
    }


    private NioClientFactory() throws IOException {
        super();
    }

    @Override
    public void addTask(NioClientTask task) {
        super.addTask(task);
        Logcat.d("==> NioClientFactory addTask mConnectCache.size = " + mConnectCache.size());
    }

    @Override
    public void close() {
        super.close();
        mFactory = null;
    }

    private SocketChannel createSocketChannel(NioClientTask task) {
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);// 设置为非阻塞
            channel.connect(task.getSocketAddress());
            task.setChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    @Override
    protected void onConnectTask(Selector selector, NioClientTask task) {
        SocketChannel channel = task.getSocketChannel();
        Logcat.d("==> NioClientFactory onConnectTask");
        if (channel == null) {
            channel = createSocketChannel(task);
        }
        try {
            if (channel.isBlocking()) {
                channel.configureBlocking(false);// 设置为非阻塞
            }
            if (channel.isConnected()) {
                task.onConnect(true);
                channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } else {
                channel.register(selector, SelectionKey.OP_CONNECT);
            }
            channel.keyFor(selector).attach(task);
        } catch (Exception e) {
            channel = createSocketChannel(task);
            try {
                channel.configureBlocking(false);// 设置为非阻塞
                channel.register(selector, SelectionKey.OP_CONNECT);
                channel.keyFor(selector).attach(task);
            } catch (Exception e1) {
                task.onConnect(false);
                mDestroyCache.add(task);
            }
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
                Logcat.d("==> selectionKey isConnectable ");
                boolean isOpen = false;
                try {
                    if (channel.isConnectionPending()) {
                        isOpen = channel.finishConnect();// 完成连接
                    } else {
                        isOpen = channel.isConnected();
                    }
                } catch (Exception e) {
                    mDestroyCache.add(task);
                    e.printStackTrace();
                } finally {
                    task.onConnect(isOpen);
                    if (isOpen) {
                        try {
                            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            channel.keyFor(selector).attach(task);
                        } catch (Exception e) {
                            mDestroyCache.add(task);
                            e.printStackTrace();
                        }
                    }
                }
            } else if (selectionKey.isValid() && selectionKey.isReadable()) {
                Logcat.d("==> selector isReadable keys = " + selector.keys().size());
                NioReceive receive = task.getReceive();
                if (receive != null) {
                    receive.onRead(channel);
                }
            } else if (selectionKey.isValid() && selectionKey.isWritable()) {
//                    System.out.println("==> selectionKey isWritable ");
                NioSender sender = task.getSender();
                if (sender != null) {
                    sender.onWrite(channel);
                }
            }
        }
        iterator.remove();// 删除已处理过的事件


    }

    @Override
    protected void onDisconnectTask(NioClientTask task) {
        task.onClose();
    }

}
