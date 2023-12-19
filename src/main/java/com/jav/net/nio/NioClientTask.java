package com.jav.net.nio;


import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.BaseNetTask;
import com.jav.net.component.DefaultCacheComponentPicker;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask extends BaseNetTask<SocketChannel> {

    protected NioSender mSender = null;

    protected NioReceiver mReceiver = null;


    public NioClientTask() {
    }

    public NioClientTask(SocketChannel channel) {
        if (!channel.isOpen() || !channel.isConnected()) {
            throw new IllegalStateException("SocketChannel is bed !!! ");
        }
        setChannel(channel);
    }

    //---------------------------- set ---------------------------------------

    public void setSender(NioSender sender) {
        this.mSender = sender;
    }

    public void setReceiver(NioReceiver receiver) {
        this.mReceiver = receiver;
    }


    //---------------------------- get ---------------------------------------


    public <T extends NioSender> T getSender() {
        return (T) mSender;
    }

    public <T extends NioReceiver> T getReceiver() {
        return (T) mReceiver;
    }

    @Override
    protected void onBeReadyChannel(SelectionKey selectionKey, SocketChannel channel) {
        super.onBeReadyChannel(selectionKey, channel);
    }

    @Override
    protected SocketChannel getChannel() {
        return super.getChannel();
    }

    @Override
    protected void onConfigChannel(SocketChannel channel) {
        super.onConfigChannel(channel);
    }

    @Override
    protected void setChannel(SocketChannel channel) {
        super.setChannel(channel);
    }

    @Override
    protected SelectionKey getSelectionKey() {
        return super.getSelectionKey();
    }

    @Override
    protected void setSelectionKey(SelectionKey key) {
        super.setSelectionKey(key);
    }

    @Override
    protected IControlStateMachine<Integer> getStatusMachine() {
        return super.getStatusMachine();
    }

    /**
     * 断开链接后回调
     */
    @Override
    protected void onRecovery() {
        super.onRecovery();
        if (mSender != null) {
            mSender.getCacheComponent().clearCache(new DefaultCacheComponentPicker());
        }
    }
}
