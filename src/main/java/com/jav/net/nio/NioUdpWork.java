package com.jav.net.nio;


import com.jav.net.base.FactoryContext;
import com.jav.net.base.NioNetWork;
import com.jav.net.base.SelectorEventHubs;
import com.jav.net.base.joggle.NetErrorType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class NioUdpWork extends NioNetWork<NioUdpTask, DatagramChannel> {

    /**
     * ip协议最大的包
     */
    private static final int MAX_IP_PACKAGE = 65536;

    public NioUdpWork(FactoryContext intent) {
        super(intent);
    }

    @Override
    protected DatagramChannel onCreateChannel(NioUdpTask netTask) throws IOException {
        DatagramChannel channel;
        if (netTask.isBroadcast()) {
            channel = DatagramChannel.open(StandardProtocolFamily.INET);
            channel.setOption(StandardSocketOptions.SO_BROADCAST, true);
        } else {
            channel = DatagramChannel.open();
        }
        return channel;
    }

    @Override
    protected void onInitChannel(NioUdpTask netTask, DatagramChannel channel) throws IOException {
        if (channel.isBlocking()) {
            // 设置为非阻塞
            channel.configureBlocking(false);
        }

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
        channel.setOption(StandardSocketOptions.SO_RCVBUF, MAX_IP_PACKAGE);
        channel.setOption(StandardSocketOptions.SO_SNDBUF, MAX_IP_PACKAGE);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_TTL, netTask.getLiveTime().getTtl());
        netTask.onConfigChannel(channel);

        if (netTask.getHost() != null) {
            InetSocketAddress address = new InetSocketAddress(netTask.getHost(), netTask.getPort());
            if (netTask.isServer()) {
                channel.bind(address);
            } else {
                channel.connect(address);
            }
        }
    }

    @Override
    protected void onRegisterChannel(NioUdpTask netTask, DatagramChannel channel) {
        try {
            SelectorEventHubs eventHubs = mFactoryContext.getSelectorEventHubs();
            SelectionKey key = eventHubs.registerReadEvent(channel, netTask);
            netTask.onBeReadyChannel(key, channel);
            netTask.setSelectionKey(key);
            netTask.setChannel(channel);
        } catch (Throwable e) {
            callChannelError(netTask, NetErrorType.CONNECT, e);
        }
    }

    @Override
    protected void onReadEvent(NioUdpTask netTask, DatagramChannel channel) {
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
    protected void onWriteEvent(NioUdpTask netTask, DatagramChannel channel) {
        NioUdpSender sender = netTask.getSender();
        if (sender != null) {
            try {
                sender.onSendNetData();
            } catch (Throwable e) {
                hasErrorToUnExecTask(netTask, e);
            }
        }
    }


    protected void hasErrorToUnExecTask(NioUdpTask netTask, Throwable e) {
//        INetTaskComponent taskFactory = mFactoryContext.getNetTaskComponent();
//        taskFactory.addUnExecTask(netTask);
//        if (!(e instanceof SocketChannelCloseException)) {
        e.printStackTrace();
//        }
    }
}
