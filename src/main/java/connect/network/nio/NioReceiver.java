package connect.network.nio;


import connect.network.base.SocketChannelCloseException;
import connect.network.base.joggle.INetReceiver;
import connect.network.xhttp.XMultiplexCacheManger;
import connect.network.xhttp.utils.MultiLevelBuf;
import util.IoEnvoy;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioReceiver {

    protected INetReceiver<MultiLevelBuf> receiver;

    public NioReceiver() {
    }

    public void setDataReceiver(INetReceiver<MultiLevelBuf> receiver) {
        this.receiver = receiver;
    }

    public void resetMultilevelBuf(MultiLevelBuf buf) {
        XMultiplexCacheManger.getInstance().lose(buf);
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        Throwable exception = null;
        MultiLevelBuf buf = XMultiplexCacheManger.getInstance().obtainBuf();
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
        notifyReceiverImp(buf, exception, ret);
    }

    protected void notifyReceiverImp(MultiLevelBuf buf, Throwable exception, long ret) throws Throwable {
        try {
            if (receiver != null) {
                receiver.onReceiveFullData(buf, exception);
            }
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
