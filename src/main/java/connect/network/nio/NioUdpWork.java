package connect.network.nio;


import connect.network.base.SocketChannelCloseException;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class NioUdpWork<T extends NioUdpTask> extends NioNetWork<T> {

    @Override
    protected void onConnectTask(T netTask) {
        DatagramChannel channel = netTask.getChannel();
        try {
            if (channel == null) {
                channel = createSocketChannel(netTask);
                netTask.getSender().setChannel(channel);
                SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_READ, netTask);
                netTask.setSelectionKey(selectionKey);
                netTask.onReadyChannel();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private DatagramChannel createSocketChannel(T netTask) throws IOException {
        DatagramChannel channel;
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
        return channel;
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
                    addDestroyTask(task);
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
     * @param task 网络请求任务
     */
    @Override
    public void onDisconnectTask(T task) {
        try {
            task.onCloseClientChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (task.getChannel() != null) {
                try {
                    task.getChannel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
