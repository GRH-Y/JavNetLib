package com.jav.net.nio;


import com.jav.common.log.LogDog;
import com.jav.net.base.FactoryContext;
import com.jav.net.base.NioNetWork;
import com.jav.net.base.SelectorEventHubs;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.NetErrorType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 非阻塞客户端事务,处理net work的生命周期事件
 *
 * @author yyz
 */
public class NioClientWork extends NioNetWork<NioClientTask, SocketChannel> {

    protected NioClientWork(FactoryContext context) {
        super(context);
    }

    //---------------------------------------------------------------------------------------------------------------


    @Override
    protected SocketChannel onCreateChannel(NioClientTask netTask) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.setOption(StandardSocketOptions.SO_LINGER, 0);
        // 禁用Nagle算法
//        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        return channel;
    }

    @Override
    protected void onInitChannel(NioClientTask netTask, SocketChannel channel) throws IOException {
        if (channel.isBlocking()) {
            // 设置为非阻塞
            channel.configureBlocking(false);
        }
        netTask.onConfigChannel(channel);
        if(channel.isConnected() || channel.isConnectionPending()){
            return;
        }
        InetSocketAddress address = new InetSocketAddress(netTask.getHost(), netTask.getPort());
        channel.connect(address);
    }

    protected SelectionKey registerReadEvent(NioClientTask netTask, SocketChannel channel) throws IOException {
        SelectorEventHubs eventHubs = mFactoryContext.getSelectorEventHubs();
        return eventHubs.registerReadEvent(channel, netTask);
    }

    private void callReadyChannel(NioClientTask netTask, SocketChannel channel, SelectionKey key) {
        netTask.setChannel(channel);
        netTask.setSelectionKey(key);
        netTask.onBeReadyChannel(key, channel);
    }


    @Override
    protected void onRegisterChannel(NioClientTask netTask, SocketChannel channel) throws IOException {
        if (channel.isConnected()) {
            SelectionKey key = registerReadEvent(netTask, channel);
            callReadyChannel(netTask, channel, key);
        } else {
            SelectorEventHubs eventHubs = mFactoryContext.getSelectorEventHubs();
            eventHubs.registerConnectEvent(channel, netTask);
        }
    }

    @Override
    protected void onConnectEvent(NioClientTask netTask, SocketChannel channel) {
        try {
            boolean isConnect = channel.finishConnect();
            if (isConnect) {
                SelectionKey key = registerReadEvent(netTask, channel);
                callReadyChannel(netTask, channel, key);
            } else {
                // 连接失败，则结束任务
                INetTaskComponent<NioClientTask> taskFactory = mFactoryContext.getNetTaskComponent();
                taskFactory.addUnExecTask(netTask);
                LogDog.e("## NioClientWork onConnectEvent task fails !!!");
            }
        } catch (Throwable e) {
            callChannelError(netTask, NetErrorType.CONNECT, e);
        }
    }

    @Override
    protected void onReadEvent(NioClientTask netTask, SocketChannel channel) {
        NioReceiver receive = netTask.getReceiver();
        if (receive != null) {
            try {
                receive.onReadNetData(channel);
            } catch (Throwable e) {
                callChannelError(netTask, NetErrorType.READ, e);
            }
        }
    }

    @Override
    protected void onWriteEvent(NioClientTask netTask, SocketChannel channel) {
        NioSender sender = netTask.getSender();
        if (sender != null) {
            try {
                sender.onSendNetData();
            } catch (Throwable e) {
                callChannelError(netTask, NetErrorType.WRITE, e);
            }
        }
    }


}
