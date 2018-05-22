package connect.network.nio;


import connect.network.nio.interfaces.INioFactorySetting;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class NioEventSetting implements INioFactorySetting {

    private SocketChannel channel;
    private Selector selector;
    private NioClientTask task;

    protected NioEventSetting(SocketChannel channel, Selector selector, NioClientTask task) {
        this.channel = channel;
        this.selector = selector;
        this.task = task;
    }


    @Override
    public void enableReadEvent() {
        try {
            channel.register(selector, SelectionKey.OP_READ);
            channel.keyFor(selector).attach(task);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disableReadEvent() {
        try {
            channel.register(selector, SelectionKey.OP_WRITE);
            channel.keyFor(selector).attach(task);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enableWriteEvent() {
        try {
            channel.register(selector, SelectionKey.OP_WRITE);
            channel.keyFor(selector).attach(task);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disableWriteEvent() {
        try {
            channel.register(selector, SelectionKey.OP_READ);
            channel.keyFor(selector).attach(task);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enableReadWriteEvent() {
        try {
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            channel.keyFor(selector).attach(task);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

}
