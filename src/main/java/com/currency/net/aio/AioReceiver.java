package com.currency.net.aio;

import com.currency.net.base.joggle.IAioNetReceiver;
import com.currency.net.ssl.TLSHandler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioReceiver {

    protected TLSHandler mTLSHandler;
    private AsynchronousSocketChannel mChannel;
    protected IAioNetReceiver mReceiver;
    private HandlerCore mHandlerCore;
    private ByteBuffer mReceiverBuffer = ByteBuffer.allocateDirect(4096);

    public AioReceiver(AsynchronousSocketChannel channel) {
        this.mChannel = channel;
        mHandlerCore = new HandlerCore();
    }

    public void setDataReceiver(IAioNetReceiver receiver) {
        this.mReceiver = receiver;
    }

    public void setChannel(AsynchronousSocketChannel channel) {
        this.mChannel = channel;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.mTLSHandler = tlsHandler;
    }

    /**
     * 触发接收数据
     */
    public void triggerReceiver() {
        if (mTLSHandler != null) {
            try {
                mTLSHandler.readAndUnwrap(mChannel, mHandlerCore, mReceiverBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mChannel.read(mReceiverBuffer, mReceiverBuffer, mHandlerCore);
        }
    }

    private class HandlerCore implements CompletionHandler<Integer, ByteBuffer> {

        @Override
        public void completed(Integer result, ByteBuffer byteBuffer) {
            if (mReceiver != null) {
                boolean ret = mReceiver.onCompleted(result, byteBuffer);
                if (!ret) {
                    return;
                }
            }
            triggerReceiver();
        }

        @Override
        public void failed(Throwable exc, ByteBuffer byteBuffer) {
            if (mReceiver != null) {
                mReceiver.onFailed(exc, byteBuffer);
            }
        }
    }

}
