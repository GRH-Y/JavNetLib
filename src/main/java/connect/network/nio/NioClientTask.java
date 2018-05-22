package connect.network.nio;


import connect.network.nio.interfaces.INioClientTask;
import connect.network.nio.interfaces.INioFactorySetting;
import connect.network.nio.interfaces.INioReceive;
import connect.network.nio.interfaces.INioSender;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask implements INioClientTask {

    protected SocketChannel channel = null;
    protected InetSocketAddress address;
    protected INioFactorySetting setting;


    protected void setAddress(String ip, int port) {
        address = new InetSocketAddress(ip, port);
    }


    @Override
    public AbstractSelectableChannel getSocketChannel() {
        return channel;
    }

    protected SocketAddress getSocketAddress() {
        return address;
    }

    @Override
    public INioSender getSender() {
        return null;
    }

    @Override
    public INioReceive getReceive() {
        return null;
    }


    /**
     * 链接状态回调
     *
     * @param isConnect
     */
    @Override
    public void onConnect(boolean isConnect) {

    }


    @Override
    public void onClose() {

    }
}
