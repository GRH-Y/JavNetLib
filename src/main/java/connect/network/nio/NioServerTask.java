package connect.network.nio;


import connect.network.nio.interfaces.INioServerTask;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

/**
 * nio服务端任务(创建服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerTask implements INioServerTask {

    protected ServerSocketChannel channel = null;
    protected InetSocketAddress address;


    protected void setAddress(String ip, int port) {
        address = new InetSocketAddress(ip, port);
    }


    /**
     * 获取该任务要链接的服务器地址
     *
     * @param channel
     */
    protected void setSocketChannel(AbstractSelectableChannel channel) {
        this.channel = (ServerSocketChannel) channel;
    }

    @Override
    public AbstractSelectableChannel getSocketChannel() {
        return channel;
    }

    @Override
    public void onAccept(SocketChannel channel) {

    }

    protected SocketAddress getSocketAddress() {
        return address;
    }

    @Override
    public void onConnect(boolean isConnect) {

    }

    @Override
    public void onClose() {

    }
}
