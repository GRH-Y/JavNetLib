package connect.network.nio;


import util.LogDog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * nio服务端工厂(单线程管理多个ServerSocket)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerFactory extends AbstractNioFactory<NioServerTask> {

    private static NioServerFactory mFactory = null;

    public static synchronized NioServerFactory getFactory() {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioServerFactory();
                }
            }
        }
        return mFactory;
    }

    public static void destroy() {
        mFactory = null;
    }

    private NioServerFactory() {
        super();
    }

    @Override
    public void addTask(NioServerTask task) {
        super.addTask(task);
        LogDog.d("==##> NioServerFactory addTask mConnectCache.size = " + mConnectCache.size());
    }


    @Override
    protected void onConnectTask(Selector selector, NioServerTask task) {
        //创建服务，并注册到selector，监听所有的事件
        LogDog.d("==##> NioServerFactory onConnectTask NioServerTask = " + task.getHost() + ":" + task.getPort());
        ServerSocketChannel serverSocketChannel = task.getSocketChannel();
        if (serverSocketChannel == null && task.getHost() != null && task.getPort() > 0) {
            try {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(task.getHost(), task.getPort()));
                task.setSocketChannel(serverSocketChannel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (serverSocketChannel == null) {
            task.onOpenServerChannel(false);
            mDestroyCache.add(task);
            return;
        }
        try {
            if (serverSocketChannel.isBlocking()) {
                serverSocketChannel.configureBlocking(false);
            }
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverSocketChannel.keyFor(selector).attach(task);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boolean isOpen = serverSocketChannel.isOpen();
            task.onOpenServerChannel(isOpen);
            if (!isOpen) {
                mDestroyCache.add(task);
            }
        }
    }


    @Override
    protected void onSelectorTask(Selector selector) {

        LogDog.d("==##> NioServerFactory onSelectorTask");

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            NioServerTask task = (NioServerTask) selectionKey.attachment();

            if (selectionKey.isValid() && selectionKey.isAcceptable()) {
                LogDog.d("==##> selectionKey isAcceptable ");
                try {
                    SocketChannel channel = serverSocketChannel.accept();
                    task.onAcceptServerChannel(channel);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        iterator.remove();// 删除已处理过的事件
    }

    @Override
    protected void onDisconnectTask(NioServerTask task) {
        task.onCloseServerChannel();
    }
}
