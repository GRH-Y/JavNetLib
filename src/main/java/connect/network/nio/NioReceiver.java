package connect.network.nio;


import connect.network.base.SocketChannelCloseException;
import connect.network.base.joggle.INetReceiver;
import connect.network.base.joggle.IReceiverDecodeHandle;
import connect.network.xhttp.XMultiplexCacheManger;
import connect.network.xhttp.utils.MultilevelBuf;
import util.IoEnvoy;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioReceiver {

    protected INetReceiver<MultilevelBuf> receiver;
    protected IReceiverDecodeHandle decodeHandle;

    public NioReceiver() {
    }

        public NioReceiver(INetReceiver<MultilevelBuf> receiver) {
        this.receiver = receiver;
    }

    public void setDataReceiver(INetReceiver<MultilevelBuf> receiver) {
        this.receiver = receiver;
    }

    public void setDecodeHandle(IReceiverDecodeHandle decodeHandle) {
        this.decodeHandle = decodeHandle;
    }

    public void resetMultilevelBuf(MultilevelBuf buf) {
        XMultiplexCacheManger.getInstance().lose(buf);
    }


    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        if (decodeHandle != null) {
            decodeHandle.onDecode(channel);
        } else {
            Throwable exception = null;
            MultilevelBuf buf = XMultiplexCacheManger.getInstance().obtainBuf();
            ByteBuffer[] buffer = buf.getAllBuf();
            long ret = IoEnvoy.FAIL;
            try {
                do {
                    ret = channel.read(buffer);
                    if (ret > 0) {
                        buf.setBackBuf(buffer);
                        buffer = buf.getAllBuf();
                    }
                } while (ret > 0);
            } catch (Throwable e) {
                exception = e;
            } finally {
                buf.setBackBuf(buffer);
                buf.flip();
            }
            try {
                notifyReceiverImp(buf, exception);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (exception != null) {
                throw exception;
            } else if (ret < 0 && exception == null) {
                throw new SocketChannelCloseException();
            }
        }
    }

    protected void notifyReceiverImp(MultilevelBuf buf, Throwable e) {
        if (receiver != null) {
            receiver.onReceiveFullData(buf, e);
        }
    }
}
