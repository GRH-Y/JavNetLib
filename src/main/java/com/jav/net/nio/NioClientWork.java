package com.jav.net.nio;


import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.common.state.joggle.IStateMachine;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.NetErrorType;
import com.jav.net.entity.FactoryContext;
import com.jav.net.ssl.TLSHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 非阻塞客户端事务,处理net work的生命周期事件
 *
 * @param <T> 网络任务
 * @param <C> 通道对象
 * @author yyz
 */
public class NioClientWork<T extends NioClientTask, C extends SocketChannel> extends AbsNioNetWork<T, C> {

    protected NioClientWork(FactoryContext context) {
        super(context);
    }

    //---------------------------------------------------------------------------------------------------------------

    /**
     * 创建通道
     *
     * @param netTask
     * @return
     */
    @Override
    protected C onCreateChannel(T netTask) throws IOException {
        InetSocketAddress address = new InetSocketAddress(netTask.getHost(), netTask.getPort());
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.setOption(StandardSocketOptions.SO_LINGER, 0);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, false);
        netTask.onConfigChannel(channel);
        channel.connect(address);
        netTask.setChannel(channel);
        return (C) channel;
    }

    @Override
    protected void onInitChannel(T netTask, C channel) throws IOException {
        if (channel.isBlocking()) {
            // 设置为非阻塞
            channel.configureBlocking(false);
        }
    }

    protected void registerEvent(T netTask, C channel) throws IOException {
        initSSLConnect(netTask);
        SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_READ, netTask);
        netTask.setSelectionKey(selectionKey);
        netTask.onBeReadyChannel(channel);
        netTask.setChannel(channel);
    }

    @Override
    protected void onRegisterChannel(T netTask, C channel) throws IOException {
        if (channel.isConnected()) {
            registerEvent(netTask, channel);
        } else {
            SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_CONNECT, netTask);
            netTask.setSelectionKey(selectionKey);
        }
    }

    @Override
    protected void onConnectEvent(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        try {
            boolean isConnect = channel.finishConnect();
            if (isConnect) {
                registerEvent(netTask, (C) channel);
            } else {
                // 连接失败，则结束任务
                INetTaskComponent taskFactory = mFactoryContext.getNetTaskComponent();
                taskFactory.addUnExecTask(netTask);
                LogDog.e("## NioClientWork onConnectEvent task fails !!!");
            }
        } catch (Throwable e) {
            IControlStateMachine<Integer> stateMachine = (IControlStateMachine<Integer>) netTask.getStatusMachine();
            stateMachine.detachState(NetTaskStatus.RUN);
            stateMachine.attachState(NetTaskStatus.IDLING);
            callChannelError(netTask, NetErrorType.CONNECT, e);
        }
    }

    @Override
    protected void onReadEvent(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
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
    protected void onWriteEvent(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        NioSender sender = netTask.getSender();
        if (sender != null) {
            try {
                sender.onSendNetData();
            } catch (Throwable e) {
                callChannelError(netTask, NetErrorType.WRITE, e);
            }
        }
    }


    /**
     * 准备断开链接回调
     *
     * @param netTask 网络请求任务
     */
    @Override
    protected void onDisconnectTask(T netTask) {
        super.onDisconnectTask(netTask);
        TLSHandler tlsHandler = netTask.getTlsHandler();
        if (tlsHandler != null) {
            tlsHandler.release();
        }
    }

}
