package connect.network.aio;

import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;
import connect.network.ssl.TLSHandler;
import log.LogDog;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioSender implements INetSender {

    protected AsynchronousSocketChannel channel;
    protected ISenderFeedback senderFeedback;
    protected TLSHandler tlsHandler;
    private HandlerCore handlerCore;

    public AioSender(AsynchronousSocketChannel channel) {
        this.channel = channel;
        handlerCore = new HandlerCore();
    }

    public void setChannel(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.tlsHandler = tlsHandler;
    }


    @Override
    public void setSenderFeedback(ISenderFeedback feedback) {
        this.senderFeedback = feedback;
    }

    @Override
    public void sendData(byte[] data) {
        if (data != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            if (tlsHandler != null) {
                try {
                    tlsHandler.wrapAndWrite(channel, byteBuffer, handlerCore);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (senderFeedback != null) {
                        senderFeedback.onSenderFeedBack(this, byteBuffer, e);
                    }
                }
            } else {
                channel.write(byteBuffer, byteBuffer, handlerCore);
            }
        }
    }

    private class HandlerCore implements CompletionHandler<Integer, ByteBuffer> {

        @Override
        public void completed(Integer result, ByteBuffer byteBuffer) {
            if (byteBuffer.hasRemaining()) {
                channel.write(byteBuffer, byteBuffer, this);
            } else {
                LogDog.d("发送数据成功");
                if (senderFeedback != null) {
                    senderFeedback.onSenderFeedBack(AioSender.this, byteBuffer, null);
                }
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer byteBuffer) {
            LogDog.e("发送数据失败");
            exc.printStackTrace();
            if (senderFeedback != null) {
                senderFeedback.onSenderFeedBack(AioSender.this, byteBuffer, exc);
            }
        }
    }
}
