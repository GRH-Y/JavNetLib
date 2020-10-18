package connect.network.aio;

import connect.network.base.joggle.INetReceiver;
import connect.network.ssl.TLSHandler;
import log.LogDog;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public class AioReceiver {

    protected TLSHandler tlsHandler;
    private AioClientTask clientTask;
    protected INetReceiver<ByteBuffer> receiver;
    private HandlerCore handlerCore;
    private ByteBuffer receiverBuffer = ByteBuffer.allocateDirect(4096);

    public AioReceiver(AioClientTask clientTask) {
        this.clientTask = clientTask;
        handlerCore = new HandlerCore();
    }

    public void setDataReceiver(INetReceiver<ByteBuffer> receiver) {
        this.receiver = receiver;
    }

    public void setClientTask(AioClientTask clientTask) {
        this.clientTask = clientTask;
    }

    public void setTlsHandler(TLSHandler tlsHandler) {
        this.tlsHandler = tlsHandler;
    }

    /**
     * 触发接收数据
     */
    public void triggerReceiver() {
        clientTask.getSocketChannel().read(receiverBuffer, receiverBuffer, handlerCore);
    }

    private class HandlerCore implements CompletionHandler<Integer, ByteBuffer> {

        @Override
        public void completed(Integer result, ByteBuffer byteBuffer) {
            if (result.intValue() == -1) {
                clientTask.getFactory().removeTask(clientTask);
            } else {
                byteBuffer.flip();
                if (receiver != null) {
                    receiver.onReceiveFullData(byteBuffer, null);
                }
                byte[] data = new byte[byteBuffer.remaining()];
                byteBuffer.get(data);
                byteBuffer.clear();
                LogDog.d("==> 接收数据 = " + new String(data));
                LogDog.d("==> 耗时 = " + (System.currentTimeMillis() - AioClientFactory.starTime));
                clientTask.getSocketChannel().read(receiverBuffer, receiverBuffer, this);
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer byteBuffer) {
            if (receiver != null) {
                receiver.onReceiveFullData(byteBuffer, exc);
            }
        }
    }

}
