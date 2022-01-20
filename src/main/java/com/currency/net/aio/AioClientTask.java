package com.currency.net.aio;

import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.ssl.TLSHandler;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;

public class AioClientTask extends BaseAioChannelTask<AsynchronousSocketChannel> {


    private INetTaskContainer mNetTaskFactory;

    private AioSender mSender;

    private AioReceiver mReceiver;

    private TLSHandler mTLSHandler = null;

    public AioClientTask() {
    }

    public AioClientTask(AsynchronousSocketChannel channel) {
        setChannel(channel);
    }


    //---------------------------------- set ---------------------------------------------

    protected void setNetTaskFactory(INetTaskContainer factory) {
        this.mNetTaskFactory = factory;
    }

    public void setSender(AioSender sender) {
        this.mSender = sender;
    }

    public void setReceiver(AioReceiver receiver) {
        this.mReceiver = receiver;
    }

    public void setTLSHandler(TLSHandler tlsHandler) {
        this.mTLSHandler = tlsHandler;
    }

    //----------------------------------- get ---------------------------------------------

    protected INetTaskContainer getFactory() {
        return mNetTaskFactory;
    }

    public AioSender getSender() {
        return mSender;
    }

    public AioReceiver getReceiver() {
        return mReceiver;
    }


    public TLSHandler getTlsHandler() {
        return mTLSHandler;
    }

    //---------------------------- on ---------------------------------------

    protected AsynchronousChannelGroup onInitChannelGroup() {
        return null;
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        this.mNetTaskFactory = null;
    }
}
