package com.currency.net.nio;


import com.currency.net.base.FactoryContext;
import com.currency.net.base.SocketChannelCloseException;
import com.currency.net.base.joggle.INetTaskContainer;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class NioUdpWork<T extends NioUdpTask, C extends DatagramChannel> extends AbsNioNetWork<T, C> {

    public NioUdpWork(FactoryContext intent) {
        super(intent);
    }

    @Override
    protected C onCreateChannel(T netTask) {
        DatagramChannel channel = null;
        try {
            if (netTask.isBroadcast()) {
                channel = DatagramChannel.open(StandardProtocolFamily.INET);
            } else {
                channel = DatagramChannel.open();
            }
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            channel.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
            netTask.onConfigChannel(channel);
            netTask.setChannel(channel);
        } catch (Throwable e) {
            callBackInitStatusChannelError(netTask, e);
        }
        return (C) channel;
    }

    @Override
    protected void onInitChannel(T netTask, C channel) {
        netTask.getSender().setChannel(channel);
    }

    @Override
    protected void onRegisterChannel(T netTask, C channel) {
        try {
            SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_READ, netTask);
            netTask.setSelectionKey(selectionKey);
            netTask.onReadyChannel();
        } catch (Throwable e) {
            callBackInitStatusChannelError(netTask, e);
        }
    }

    @Override
    protected void onSelectionKey(SelectionKey selectionKey) {
        DatagramChannel channel = (DatagramChannel) selectionKey.channel();
        if (channel == null) {
            return;
        }
        T task = (T) selectionKey.attachment();
        boolean isCanRead = selectionKey.isValid() && selectionKey.isReadable();

        if (isCanRead) {
            NioUdpReceiver receiver = task.getReceiver();
            if (receiver != null) {
                try {
                    receiver.onReadNetData(channel);
                } catch (Throwable e) {
                    INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
                    taskFactory.addUnExecTask(task);
                    if (!(e instanceof SocketChannelCloseException)) {
                        e.printStackTrace();
                    }
                }
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
            netTask.onCloseClientChannel();
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
}
