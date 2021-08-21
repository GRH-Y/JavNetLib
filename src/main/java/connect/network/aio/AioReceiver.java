package connect.network.aio;

import connect.network.base.joggle.INetReceiver;
import connect.network.ssl.TLSHandler;
import log.LogDog;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class AioReceiver {

    protected TLSHandler mTLSHandler;
    private AioClientTask mClientTask;
    protected INetReceiver<ByteBuffer> mReceiver;
    private HandlerCore mHandlerCore;
    private ByteBuffer mReceiverBuffer = ByteBuffer.allocateDirect(4096);

    public AioReceiver(AioClientTask clientTask) {
        this.mClientTask = clientTask;
        mHandlerCore = new HandlerCore();
    }

    public void setDataReceiver(INetReceiver<ByteBuffer> receiver) {
        this.mReceiver = receiver;
    }

    public void setClientTask(AioClientTask clientTask) {
        this.mClientTask = clientTask;
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
                mTLSHandler.readAndUnwrap(mClientTask.getChannel(), mHandlerCore, mReceiverBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mClientTask.getChannel().read(mReceiverBuffer, mReceiverBuffer, mHandlerCore);
        }
    }

    private class HandlerCore implements CompletionHandler<Integer, ByteBuffer> {

        @Override
        public void completed(Integer result, ByteBuffer byteBuffer) {
            if (result.intValue() == -1) {
                mClientTask.getFactory().removeTask(mClientTask);
            } else {
                byteBuffer.flip();
                byte[] data = new byte[byteBuffer.remaining()];
                byteBuffer.get(data);
                byteBuffer.clear();
                LogDog.d("==> 接收数据 = " + new String(data));
                if (mReceiver != null) {
                    mReceiver.onReceiveFullData(byteBuffer, null);
                }
                triggerReceiver();
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer byteBuffer) {
            if (mReceiver != null) {
                mReceiver.onReceiveFullData(byteBuffer, exc);
            }
        }
    }

}
