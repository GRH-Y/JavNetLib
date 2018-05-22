package connect.network.nio;


import connect.network.nio.interfaces.INioReceive;
import connect.network.nio.interfaces.INioSender;
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
public class NioSocketFactory extends AbstractNioFactory<NioClientTask> {

    private static NioSocketFactory factory = null;


    public static NioSocketFactory getFactory() throws IOException {
        if (factory == null) {
            synchronized (NioSocketFactory.class) {
                if (factory == null) {
                    factory = new NioSocketFactory();
                }
            }
        }
        return factory;
    }


    private NioSocketFactory() throws IOException {
        super();
    }

    @Override
    public void addNioTask(NioClientTask task) {
        super.addNioTask(task);
        Logcat.d("==> NioSocketFactory addNioTask connectCache.size = " + connectCache.size());
    }

    @Override
    public void close() {
        super.close();
        factory = null;
    }

    private SocketChannel createSocketChannel(NioClientTask task) {
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);// 设置为非阻塞
            channel.connect(task.getSocketAddress());
            task.channel = channel;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    @Override
    protected void onHasTask(Selector selector, NioClientTask task) {
        SocketChannel channel = (SocketChannel) task.getSocketChannel();
        Logcat.d("==> NioSocketFactory onHasTask");
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
            task.setting = new NioEventSetting(channel, selector, task);
            channel.keyFor(selector).attach(task);
        } catch (Exception e) {
            channel = createSocketChannel(task);
            try {
                channel.configureBlocking(false);// 设置为非阻塞
                channel.register(selector, SelectionKey.OP_CONNECT);
                channel.keyFor(selector).attach(task);
            } catch (Exception e1) {
                destroyCache.add(task);
            }
            e.printStackTrace();
        }
    }

    @Override
    protected void onSelector(Selector selector) {

//        Logcat.d("==> NioSocketFactory onSelector");

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
                    destroyCache.add(task);
                    e.printStackTrace();
                } finally {
                    task.onConnect(isOpen);
                    if (isOpen) {
                        try {
                            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                            channel.keyFor(selector).attach(task);
                        } catch (Exception e) {
                            destroyCache.add(task);
                            e.printStackTrace();
                        }
                    }
                }
            } else if (selectionKey.isValid() && selectionKey.isReadable()) {
                Logcat.d("==> selector isReadable keys = " + selector.keys().size());
                INioReceive receive = task.getReceive();
                if (receive != null) {
                    receive.read(channel);
                }
            } else if (selectionKey.isValid() && selectionKey.isWritable()) {
//                    System.out.println("==> selectionKey isWritable ");
                INioSender sender = task.getSender();
                if (sender != null) {
                    sender.write(channel);
                }
            }
        }
        iterator.remove();// 删除已处理过的事件


    }

}
