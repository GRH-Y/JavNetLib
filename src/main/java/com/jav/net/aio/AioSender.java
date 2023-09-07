package com.jav.net.aio;

import com.jav.net.base.joggle.INetSender;
import com.jav.net.base.joggle.ISenderFeedback;
import com.jav.net.ssl.TLSHandler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioSender implements INetSender<ByteBuffer> {

    protected AsynchronousSocketChannel mChannel;
    protected TLSHandler mTLSHandler;
    protected ISenderFeedback<ByteBuffer> mFeedback;
    private final HandlerCore mHandlerCore;

    public AioSender(AsynchronousSocketChannel channel) {
        this.mChannel = channel;
        mHandlerCore = new HandlerCore();
    }

    @Override
    public void setSenderFeedback(ISenderFeedback<ByteBuffer> feedback) {
        this.mFeedback = feedback;
    }

    public void setChannel(AsynchronousSocketChannel channel) {
        this.mChannel = channel;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.mTLSHandler = tlsHandler;
    }

    @Override
    public void sendData(ByteBuffer data) {
        if (data == null) {
            return;
        }
        if (mTLSHandler != null) {
            try {
                mTLSHandler.wrapAndWrite(mChannel, data, mHandlerCore);
            } catch (Exception e) {
                e.printStackTrace();
                if (mFeedback != null) {
                    mFeedback.onSenderFeedBack(this, data, e);
                }
            }
        } else {
            try {
                mChannel.write(data, data, mHandlerCore);
            } catch (Exception e) {
                e.printStackTrace();
                if (mFeedback != null) {
                    mFeedback.onSenderFeedBack(this, data, e);
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
                    if (mFeedback != null) {
                        mFeedback.onSenderFeedBack(AioSender.this, byteBuffer, e);
                    }
                }
            } else {
//                LogDog.d("发送数据成功");
                if (mFeedback != null) {
                    mFeedback.onSenderFeedBack(AioSender.this, byteBuffer, null);
                }
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer byteBuffer) {
//            LogDog.e("发送数据失败");
//            exc.printStackTrace();
            if (mFeedback != null) {
                mFeedback.onSenderFeedBack(AioSender.this, byteBuffer, exc);
            }
        }
    }
}
