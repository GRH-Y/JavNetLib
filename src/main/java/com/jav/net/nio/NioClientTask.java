package com.jav.net.nio;


import com.jav.net.component.DefaultCacheComponentPicker;
import com.jav.net.ssl.TLSHandler;

import java.nio.channels.SocketChannel;

/**
 * nio客户端任务(创建连接服务端任务)
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientTask extends BaseNioSelectionTask<SocketChannel> {

    protected NioSender mSender = null;

    protected NioReceiver mReceiver = null;

    protected TLSHandler mTLSHandler = null;

    public NioClientTask() {
    }

    public NioClientTask(SocketChannel channel, TLSHandler tlsHandler) {
        if (!channel.isOpen() || !channel.isConnected()) {
            throw new IllegalStateException("SocketChannel is bed !!! ");
        }
        setChannel(channel);
        this.mTLSHandler = tlsHandler;
    }

    //---------------------------- set ---------------------------------------

    public void setSender(NioSender sender) {
        this.mSender = sender;
    }

    public void setReceiver(NioReceiver receiver) {
        this.mReceiver = receiver;
    }

    public void setTLSHandler(TLSHandler tlsHandler) {
        this.mTLSHandler = tlsHandler;
    }

    //---------------------------- get ---------------------------------------

    public TLSHandler getTlsHandler() {
        return mTLSHandler;
    }

    public <T extends NioSender> T getSender() {
        return (T) mSender;
    }

    public <T extends NioReceiver> T getReceiver() {
        return (T) mReceiver;
    }


    /**
     * 断开链接后回调
     */
    @Override
    protected void onRecovery() {
        super.onRecovery();
        mTLSHandler = null;
        if (mSender != null) {
            mSender.getCacheComponent().clearCache(new DefaultCacheComponentPicker());
        }
    }
}
