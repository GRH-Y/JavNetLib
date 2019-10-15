package connect.network.nio;


import connect.network.base.AbstractNioNetFactory;
import connect.network.base.joggle.INetFactory;
import log.LogDog;
import util.StringEnvoy;

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
public class NioServerFactory extends AbstractNioNetFactory<NioServerTask> {

    private static NioServerFactory mFactory = null;

    public static synchronized INetFactory<NioServerTask> getFactory() {
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
        ServerSocketChannel channel = task.getServerSocketChannel();
        if (channel == null) {
            channel = createChannel(task);
        }
        if (channel != null) {
            //配置通道
            configChannel(task, channel);
            try {
                task.onConfigServer(!task.isTaskNeedClose(), task.isTaskNeedClose() ? null : channel);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (task.isTaskNeedClose()) {
                return;
            }
            //注册通道
            registerChannel(task, channel);
        }
    }

    private ServerSocketChannel createChannel(NioServerTask task) {
        if (StringEnvoy.isEmpty(task.getServerHost()) || task.getServerPort() < 0) {
            LogDog.e("## server host or port is Illegal = " + task.getServerHost() + ":" + task.getServerPort());
            removeTaskInside(task,false);
            return null;
        }
        ServerSocketChannel channel;
        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(task.getServerHost(), task.getServerPort()), task.getMaxConnect());
        } catch (Throwable e) {
            channel = null;
            removeTaskInside(task, false);
            e.printStackTrace();
        }
        return channel;
    }

    private void configChannel(NioServerTask task, ServerSocketChannel channel) {
        try {
            task.setServerSocketChannel(channel);
            channel.socket().setSoTimeout(task.getAcceptTimeout());
        } catch (Exception e) {
            removeTaskInside(task, false);
            e.printStackTrace();
        }
    }

    private void registerChannel(NioServerTask task, ServerSocketChannel channel) {
        try {
            if (channel.isOpen()) {
                //注册监听链接事件
                SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_ACCEPT, task);
                task.setSelectionKey(selectionKey);
            }
        } catch (Throwable e) {
            removeTaskInside(task, false);
            e.printStackTrace();
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
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDisconnectTask(NioServerTask task) {
        try {
            task.onCloseServerChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRecoveryTask(NioServerTask task) {
        try {
            task.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
