package connect.network.nio;

import log.LogDog;
import util.StringEnvoy;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioServerWork extends NioNetWork<NioServerTask> {


    private NioServerFactory mFactory;

    protected NioServerWork(NioServerFactory factory) {
        super();
        if (factory == null) {
            throw new IllegalArgumentException("NioServerWork factory can not be null !");
        }
        this.mFactory = factory;
    }

    @Override
    public void onConnectTask(NioServerTask task) {
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
            mFactory.removeTask(task);
            return null;
        }
        ServerSocketChannel channel;
        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(task.getServerHost(), task.getServerPort()), task.getMaxConnect());
        } catch (Throwable e) {
            channel = null;
            mFactory.removeTask(task);
            e.printStackTrace();
        }
        return channel;
    }

    private void configChannel(NioServerTask task, ServerSocketChannel channel) {
        try {
            task.setServerSocketChannel(channel);
            channel.socket().setSoTimeout(task.getAcceptTimeout());
        } catch (Exception e) {
            mFactory.removeTask(task);
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
            mFactory.removeTask(task);
            e.printStackTrace();
        }
    }

    @Override
    public void onSelectionKey(SelectionKey selectionKey) {
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
    public void onDisconnectTask(NioServerTask task) {
        try {
            task.onCloseServerChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            task.setServerSocketChannel(null);
        }
    }

}
