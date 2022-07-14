package com.jav.net.nio;


import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * nio服务端任务(创建服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerTask extends BaseNioSelectionTask<ServerSocketChannel> {

    private int mMaxConnect = 50;

    public NioServerTask() {
    }

    public NioServerTask(ServerSocketChannel channel) {
        mChannel = channel;
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
}
