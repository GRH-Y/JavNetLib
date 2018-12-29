package connect.network.nio;


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
    protected void onConnectTask(Selector selector, NioServerTask task) {
        //创建服务，并注册到selector，监听所有的事件
        ServerSocketChannel serverSocketChannel = task.getSocketChannel();
        if (serverSocketChannel == null && task.getServerHost() != null && task.getServerPort() > 0) {
            try {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(new InetSocketAddress(task.getServerHost(), task.getServerPort()));
                task.setSocketChannel(serverSocketChannel);
            } catch (Exception e) {
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
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            serverSocketChannel.keyFor(selector).attach(task);
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
    protected void onSelectorTask(Selector selector) {
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
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
        iterator.remove();// 删除已处理过的事件
    }

    @Override
    protected void onDisconnectTask(NioServerTask task) {
        task.onCloseServerChannel();
    }
}
