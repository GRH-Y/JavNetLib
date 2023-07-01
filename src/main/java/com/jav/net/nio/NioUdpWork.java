package com.jav.net.nio;


import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.SocketChannelCloseException;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.NetErrorType;
import com.jav.net.entity.FactoryContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class NioUdpWork<T extends NioUdpTask, C extends DatagramChannel> extends AbsNioNetWork<T, C> {

    public NioUdpWork(FactoryContext intent) {
        super(intent);
    }

    @Override
    protected C onCreateChannel(T netTask) throws IOException {
        DatagramChannel channel;
        if (netTask.isBroadcast()) {
            channel = DatagramChannel.open(StandardProtocolFamily.INET);
        } else {
            channel = DatagramChannel.open();
        }
        channel.configureBlocking(false);
        /**
         * Option Name	        描述
         * SO_SNDBUF	        The size of the socket send buffer
         * SO_RCVBUF	        The size of the socket receive buffer
         * SO_REUSEADDR	        Re-use address
         * SO_BROADCAST	        Allow transmission of broadcast datagrams
         * IP_TOS	            The Type of Service (ToS) octet in the Internet Protocol (IP) header
         * IP_MULTICAST_IF	    The network interface for Internet Protocol (IP) multicast datagrams
         * IP_MULTICAST_TTL	    The time-to-live for Internet Protocol (IP) multicast datagrams
         * IP_MULTICAST_LOOP	Loopback for Internet Protocol (IP) multicast datagrams
         */
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.setOption(StandardSocketOptions.SO_BROADCAST, true);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, netTask.getLiveTime().getTtl());
        netTask.onConfigChannel(channel);
        InetSocketAddress address = new InetSocketAddress(netTask.getHost(), netTask.getPort());
        if (netTask.isServer()) {
            channel.bind(address);
        } else {
            channel.connect(address);
        }
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

    @Override
    protected void onRegisterChannel(T netTask, C channel) {
        try {
            SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_READ, netTask);
            netTask.setSelectionKey(selectionKey);
            netTask.onBeReadyChannel(channel);
        } catch (Throwable e) {
            IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
            stateMachine.detachState(NetTaskStatus.RUN);
            stateMachine.attachState(NetTaskStatus.IDLING);
            callChannelError(netTask, NetErrorType.CONNECT, e);
        }
    }

    @Override
    protected void onReadEvent(SelectionKey key) {
        DatagramChannel channel = (DatagramChannel) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        NioUdpReceiver receive = netTask.getReceiver();
        if (receive != null) {
            try {
                receive.onReadNetData(channel);
            } catch (Throwable e) {
                hasErrorToUnExecTask(netTask, e);
            }
        }
    }

    @Override
    protected void onWriteEvent(SelectionKey key) {
        DatagramChannel channel = (DatagramChannel) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        NioUdpSender sender = netTask.getSender();
        if (sender != null) {
            try {
                sender.onSendNetData();
            } catch (Throwable e) {
                hasErrorToUnExecTask(netTask, e);
            }
        }
    }

    /**
     * 准备断开链接回调
     *
     * @param netTask 网络请求任务
     */
    @Override
    public void onDisconnectTask(T netTask) {
        try {
            netTask.onCloseChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (netTask.getChannel() != null) {
                try {
                    netTask.getChannel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void hasErrorToUnExecTask(T netTask, Throwable e) {
        INetTaskComponent taskFactory = mFactoryContext.getNetTaskComponent();
        taskFactory.addUnExecTask(netTask);
        if (!(e instanceof SocketChannelCloseException)) {
            e.printStackTrace();
        }
    }
}
