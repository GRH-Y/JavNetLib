package connect.network.nio;


import log.LogDog;
import util.StringEnvoy;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioClientWork<T extends NioClientTask> extends NioNetWork<T> {


    private NioClientFactory mFactory;

    protected NioClientWork(NioClientFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("NioClientWork factory can not be null !");
        }
        this.mFactory = factory;
    }


    //---------------------------------------------------------------------------------------------------------------

    /**
     * 准备链接
     *
     * @param task
     */
    @Override
    public void onConnectTask(T task) {
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
     * 创建通道
     *
     * @param task
     * @return
     */
    private SocketChannel createSocketChannel(T task) {
        if (StringEnvoy.isEmpty(task.getHost()) || task.getPort() < 0) {
            LogDog.e("## host or port is Illegal = " + task.getHost() + ":" + task.getPort());
            mFactory.removeTaskInside(task, false);
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
            mFactory.removeTaskInside(task, false);
            e.printStackTrace();
        }
        return channel;
    }

    /**
     * 配置通道
     *
     * @param task
     * @param channel
     */
    private void configChannel(T task, SocketChannel channel) {
        Socket socket = channel.socket();
        try {
            //复用端口
            socket.setReuseAddress(true);
            //设置超时时间
            socket.setSoTimeout(task.getConnectTimeout());
            //关闭Nagle算法
            if (task.getConnectTimeout() > 0) {
                try {
                    socket.setTcpNoDelay(true);
                } catch (Exception e) {
                    LogDog.e("The socket does not support setTcpNoDelay() !!! " + e.getMessage());
//                e.printStackTrace();
                }
            }
            //执行Socket的close方法，该方法也会立即返回
            socket.setSoLinger(true, 0);
            if (channel.isBlocking()) {
                // 设置为非阻塞
                channel.configureBlocking(false);
            }
        } catch (Throwable e) {
            mFactory.removeTaskInside(task, false);
            e.printStackTrace();
        }
    }

    /**
     * 注册读事件
     *
     * @param task
     * @param channel
     */
    private void registerChannel(T task, SocketChannel channel) {
        if (task.isTaskNeedClose()) {
            return;
        }
        try {
            if (channel.isConnected()) {
                if (task.getSender() != null) {
                    task.getSender().setChannel(channel);
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
            mFactory.removeTaskInside(task, false);
            e.printStackTrace();
        }
    }


    /**
     * 处理通道事件
     *
     * @param selectionKey 选择子
     */
    @Override
    public void onSelectionKey(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        T task = (T) selectionKey.attachment();

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
                                mFactory.removeTaskInside(task, false);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                isConnect = false;
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
                    mFactory.removeTaskInside(task, false);
                }
            }
        } else if (selectionKey.isValid() && selectionKey.isReadable()) {
            NioReceive receive = task.getReceive();
            if (receive != null) {
                try {
                    receive.onRead();
                } catch (Throwable e) {
                    mFactory.removeTaskInside(task, false);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 准备断开链接回调
     *
     * @param task 网络请求任务
     */
    @Override
    public void onDisconnectTask(T task) {
        try {
            task.onCloseSocketChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            task.setChannel(null);
        }
    }

}
