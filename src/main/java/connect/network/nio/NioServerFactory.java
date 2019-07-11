package connect.network.nio;


import connect.network.base.AbstractNioFactory;
import connect.network.base.joggle.IFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * nio服务端工厂(单线程管理多个ServerSocket)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerFactory extends AbstractNioFactory<NioServerTask> {

    private static NioServerFactory mFactory = null;

    public static synchronized IFactory<NioServerTask> getFactory() {
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
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }


    private NioServerFactory() {
        super();
    }

    @Override
    protected void onConnectTask(NioServerTask task) {
        //创建服务，并注册到selector，监听所有的事件
        ServerSocketChannel serverSocketChannel = task.getSocketChannel();
        if (serverSocketChannel == null && task.getServerHost() != null && task.getServerPort() > 0) {
            try {
                serverSocketChannel = ServerSocketChannel.open();
                task.onConfigServer(serverSocketChannel);
                serverSocketChannel.bind(new InetSocketAddress(task.getServerHost(), task.getServerPort()), task.getMaxConnect());
                task.setSocketChannel(serverSocketChannel);
            } catch (Exception e) {
                serverSocketChannel = null;
                e.printStackTrace();
            }
        }
        if (serverSocketChannel == null) {
            task.onOpenServerChannel(false);
            removeTask(task);
            return;
        }
        try {
            if (serverSocketChannel.isBlocking()) {
                serverSocketChannel.configureBlocking(false);
            }
            serverSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
            serverSocketChannel.keyFor(mSelector).attach(task);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boolean isOpen = serverSocketChannel.isOpen();
            task.onOpenServerChannel(isOpen);
            if (!isOpen) {
                removeTask(task);
            }
        }
    }


    @Override
    protected void onSelectionKey(SelectionKey selectionKey) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        NioServerTask task = (NioServerTask) selectionKey.attachment();
        if (selectionKey.isValid() && selectionKey.isAcceptable()) {
            try {
                SocketChannel channel = serverSocketChannel.accept();
                task.onAcceptServerChannel(channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDisconnectTask(NioServerTask task) {
        try {
            task.onCloseServerChannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRecoveryTask(NioServerTask task) {
        try {
            task.onRecovery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
