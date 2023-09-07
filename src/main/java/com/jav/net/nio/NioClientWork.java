package com.jav.net.nio;


import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.FactoryContext;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.base.joggle.NetErrorType;
import com.jav.net.ssl.TLSHandler;

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
public class NioClientWork extends AbsNioNetWork<NioClientTask, SocketChannel> {

    protected NioClientWork(FactoryContext context) {
        super(context);
    }

    //---------------------------------------------------------------------------------------------------------------


    protected void initSSLConnect(NioClientTask netTask) {
        if (netTask.isTls()) {
            ISSLComponent sslFactory = mFactoryContext.getSSLFactory();
            netTask.onCreateSSLContext(sslFactory);
        }
    }


    @Override
    protected SocketChannel onCreateChannel(NioClientTask netTask) throws IOException {
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
        return channel;
    }

    @Override
    protected void onInitChannel(NioClientTask netTask, SocketChannel channel) throws IOException {
        if (channel.isBlocking()) {
            // 设置为非阻塞
            channel.configureBlocking(false);
        }
    }

    protected void registerEvent(NioClientTask netTask, SocketChannel channel) throws IOException {
        initSSLConnect(netTask);
        SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_READ, netTask);
        netTask.setSelectionKey(selectionKey);
        netTask.onBeReadyChannel(channel);
        netTask.setChannel(channel);
    }

    @Override
    protected void onRegisterChannel(NioClientTask netTask, SocketChannel channel) throws IOException {
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
        NioClientTask netTask = (NioClientTask) key.attachment();
        try {
            boolean isConnect = channel.finishConnect();
            if (isConnect) {
                registerEvent(netTask, channel);
            } else {
                // 连接失败，则结束任务
                INetTaskComponent<NioClientTask> taskFactory = mFactoryContext.getNetTaskComponent();
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
        NioClientTask netTask = (NioClientTask) key.attachment();
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
        NioClientTask netTask = (NioClientTask) key.attachment();
        NioSender sender = netTask.getSender();
        if (sender != null) {
            try {
                sender.onSendNetData();
            } catch (Throwable e) {
                callChannelError(netTask, NetErrorType.WRITE, e);
            }
        }
    }


    @Override
    public boolean onDisconnectTask(NioClientTask netTask) {
        try {
            netTask.onCloseChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (netTask.getSelectionKey() != null) {
                try {
                    netTask.getSelectionKey().cancel();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (netTask.getChannel() != null) {
                try {
                    netTask.getChannel().shutdownOutput();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    netTask.getChannel().shutdownInput();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    netTask.getChannel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            TLSHandler tlsHandler = netTask.getTlsHandler();
            if (tlsHandler != null) {
                tlsHandler.release();
            }
        }
        try {
            netTask.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }

}
