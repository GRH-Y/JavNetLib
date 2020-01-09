package connect.network.nio;


import log.LogDog;
import util.StringEnvoy;

import java.io.IOException;
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
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(task.getHost(), task.getPort()));
            task.setChannel(channel);
        } catch (Throwable e) {
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
//            if (task.getConnectTimeout() > 0) {
//                try {
//                    socket.setTcpNoDelay(true);
//                } catch (Exception e) {
//                    LogDog.e("The socket does not support setTcpNoDelay() !!! " + e.getMessage());
////                e.printStackTrace();
//                }
//            }
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
        if (!channel.isOpen()) {
            return;
        }
        try {
            if (channel.isConnected()) {
                if (task.getSender() != null) {
                    //不注册发送事件，只要连接成功发送数据事件随时可以发生，也避免了Selector空转
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
            }
            SelectionKey selectionKey = channel.register(mSelector, channel.isConnected() ? SelectionKey.OP_READ : SelectionKey.OP_CONNECT, task);
            task.setSelectionKey(selectionKey);
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
        if (task.isTaskNeedClose()) {
            return;
        }

        boolean isConnectable = selectionKey.isValid() && selectionKey.isConnectable();
        boolean isReadable = selectionKey.isValid() && selectionKey.isReadable();
        if (isConnectable) {
            boolean isConnect = false;
            try {
                isConnect = channel.finishConnect();
                if (isConnect) {
                    registerChannel(task, channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (isConnect == false) {
                    try {
                        task.onConfigSocket(false, channel);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    mFactory.removeTaskInside(task, false);
                }
            }
        } else if (isReadable) {
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
