package connect.network.nio;

import connect.network.base.AbstractNioNetFactory;
import connect.network.base.NioEngine;
import log.LogDog;
import util.StringEnvoy;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * nio客户端工厂(单线程管理多个Socket)
 */
public class NioSimpleClientFactory extends AbstractNioNetFactory<NioClientTask> {

    protected NioSimpleClientFactory() {
        super();
    }

    protected NioSimpleClientFactory(NioEngine engine) {
        super(engine);
    }

    @Override
    protected void onConnectTask(NioClientTask task) {
        SocketChannel channel = task.getSocketChannel();
        if (channel == null) {
            channel = createSocketChannel(task);
        }
        if (channel != null) {
            configChannel(task, channel);
            registerChannel(task, channel);
        }
    }

    /**
     * 注册读事件
     *
     * @param task
     * @param channel
     */
    private void registerChannel(NioClientTask task, SocketChannel channel) {
        if (task.isTaskNeedClose()) {
            return;
        }
        try {
            if (channel.isConnected()) {
                if (task.getSender() != null) {
                    task.getSender().setChannel(channel);
                    task.getSender().setClientTask(task);
//                  channel.register(mSelector, SelectionKey.OP_WRITE, task);
                }
                if (task.getReceive() != null) {
                    task.getReceive().setChannel(channel);
                }
                try {
                    task.onConfigSocket(true, channel);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_READ, task);
                task.setSelectionKey(selectionKey);
            } else {
                SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_CONNECT, task);
                task.setSelectionKey(selectionKey);
            }
        } catch (Throwable e) {
            removeTaskInside(task, false);
            e.printStackTrace();
        }
    }

    private SocketChannel createSocketChannel(NioClientTask task) {
        if (StringEnvoy.isEmpty(task.getHost()) || task.getPort() < 0) {
            LogDog.e("## host or port is Illegal = " + task.getHost() + ":" + task.getPort());
            removeTaskInside(task, false);
            return null;
        }
        SocketChannel channel;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(task.getHost(), task.getPort()));
            task.setChannel(channel);
        } catch (Throwable e) {
            channel = null;
            removeTaskInside(task, false);
            e.printStackTrace();
        }
        return channel;
    }

    private void configChannel(NioClientTask task, SocketChannel channel) {
        Socket socket = channel.socket();
        try {
            //复用端口
            socket.setReuseAddress(true);
            //设置超时时间
            socket.setSoTimeout(task.getConnectTimeout());
            //关闭Nagle算法
//        if (task.getConnectTimeout() > 0 && task.getPort() == 443) {
//            try {
//                socket.setTcpNoDelay(true);
//            } catch (Exception e) {
//                LogDog.e("The socket does not support setTcpNoDelay() !!! ");
////                e.printStackTrace();
//            }
//        }
            //执行Socket的close方法，该方法也会立即返回
            socket.setSoLinger(true, 0);
            if (channel.isBlocking()) {
                // 设置为非阻塞
                channel.configureBlocking(false);
            }
        } catch (Throwable e) {
            removeTaskInside(task, false);
            e.printStackTrace();
        }
    }


    @Override
    protected void onSelectionKey(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        NioClientTask task = (NioClientTask) selectionKey.attachment();

        if (selectionKey.isValid() && selectionKey.isConnectable()) {
            boolean isConnect = channel.isConnected();
            try {
                //如果通道正在进行连接操作
                if (channel.isConnectionPending()) {
                    // 完成连接
                    isConnect = channel.finishConnect();
                    if (!isConnect && task.getConnectTimeout() > 0) {
                        long startTime = System.currentTimeMillis();
                        while (!channel.finishConnect()) {
                            if (System.currentTimeMillis() - startTime < task.getConnectTimeout()) {
                                Thread.sleep(100);
                            } else {
                                LogDog.e("connect " + task.getHost() + ":" + task.getPort() + " timeout !!!");
                                removeTaskInside(task, false);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                removeTaskInside(task, false);
                e.printStackTrace();
            } finally {
                if (isConnect) {
                    registerChannel(task, channel);
                } else {
                    try {
                        task.onConfigSocket(false, channel);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (selectionKey.isValid() && selectionKey.isReadable()) {
            NioReceive receive = task.getReceive();
            if (receive != null) {
                try {
                    receive.onRead();
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
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            task.setChannel(null);
        }
    }

    @Override
    protected void onRecoveryTask(NioClientTask task) {
        try {
            task.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
