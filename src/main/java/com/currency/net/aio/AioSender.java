package com.currency.net.aio;

import com.currency.net.base.joggle.INetSender;
import com.currency.net.base.joggle.ISenderFeedback;
import com.currency.net.ssl.TLSHandler;
import log.LogDog;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioSender implements INetSender {

    protected AsynchronousSocketChannel mChannel;
    protected ISenderFeedback mSenderFeedback;
    protected TLSHandler mTLSHandler;
    private HandlerCore mHandlerCore;

    public AioSender(AsynchronousSocketChannel channel) {
        this.mChannel = channel;
        mHandlerCore = new HandlerCore();
    }

    public void setChannel(AsynchronousSocketChannel channel) {
        this.mChannel = channel;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.mTLSHandler = tlsHandler;
    }


    @Override
    public void setSenderFeedback(ISenderFeedback feedback) {
        this.mSenderFeedback = feedback;
    }

    @Override
    public void sendData(Object objData) {
        if (objData instanceof byte[]) {
            byte[] data = (byte[]) objData;
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            if (mTLSHandler != null) {
                try {
                    mTLSHandler.wrapAndWrite(mChannel, byteBuffer, mHandlerCore);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mSenderFeedback != null) {
                        mSenderFeedback.onSenderFeedBack(this, byteBuffer, e);
                    }
                }
            } else {
                try {
                    mChannel.write(byteBuffer, byteBuffer, mHandlerCore);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mSenderFeedback != null) {
                        mSenderFeedback.onSenderFeedBack(this, byteBuffer, e);
                    }
                }
            }
        }
    }

    private class HandlerCore implements CompletionHandler<Integer, ByteBuffer> {

        @Override
        public void completed(Integer result, ByteBuffer byteBuffer) {
            if (byteBuffer.hasRemaining()) {
                try {
                    mChannel.write(byteBuffer, byteBuffer, this);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mSenderFeedback != null) {
                        mSenderFeedback.onSenderFeedBack(AioSender.this, byteBuffer, e);
                    }
                }
            } else {
                LogDog.d("发送数据成功");
                if (mSenderFeedback != null) {
                    mSenderFeedback.onSenderFeedBack(AioSender.this, byteBuffer, null);
                }
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer byteBuffer) {
            LogDog.e("发送数据失败");
            exc.printStackTrace();
            if (mSenderFeedback != null) {
                mSenderFeedback.onSenderFeedBack(AioSender.this, byteBuffer, exc);
            }
        }
    }
}
