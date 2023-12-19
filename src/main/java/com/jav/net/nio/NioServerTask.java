package com.jav.net.nio;


import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.BaseNetTask;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * nio服务端任务(创建服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerTask extends BaseNetTask<ServerSocketChannel> {

    private int mMaxConnect = 50;

    public NioServerTask() {
    }

    public NioServerTask(ServerSocketChannel channel) {
        setChannel(channel);
    }

    //---------------------------- set ---------------------------------------

    public void setMaxConnect(int maxConnect) {
        this.mMaxConnect = maxConnect;
    }

    //---------------------------- get ---------------------------------------

    public int getMaxConnect() {
        return mMaxConnect;
    }

    //---------------------------- on ---------------------------------------

    protected void onAcceptServerChannel(SocketChannel channel) {
    }

    @Override
    protected void onBeReadyChannel(SelectionKey selectionKey, ServerSocketChannel channel) {
        super.onBeReadyChannel(selectionKey, channel);
    }

    @Override
    protected void setSelectionKey(SelectionKey key) {
        super.setSelectionKey(key);
    }

    @Override
    protected void onConfigChannel(ServerSocketChannel channel) {
        super.onConfigChannel(channel);
    }

    @Override
    protected void setChannel(ServerSocketChannel channel) {
        super.setChannel(channel);
    }

    @Override
    protected IControlStateMachine<Integer> getStatusMachine() {
        return super.getStatusMachine();
    }
}
