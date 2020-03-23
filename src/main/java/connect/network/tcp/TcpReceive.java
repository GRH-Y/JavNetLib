package connect.network.tcp;


import connect.network.base.joggle.INetReceive;
import util.IoEnvoy;

import java.io.InputStream;

public class TcpReceive {

    protected INetReceive receive;
    protected InputStream stream = null;

    public TcpReceive(INetReceive receive) {
        this.receive = receive;
    }

    protected void setStream(InputStream stream) {
        this.stream = stream;
    }

    public InputStream getStream() {
        return stream;
    }

    protected void onRead() throws Exception {
        byte[] data = null;
        Exception exception = null;
        try {
            data = IoEnvoy.tryRead(stream);
        } catch (Exception e) {
            exception = e;
        } finally {
            notifyReceiver(data, exception);
        }
    }

    protected void notifyReceiver(Object data, Exception exception) throws Exception {
        if (receive != null) {
            receive.onReceive(data, exception);
        } else {
            if (exception != null) {
                throw exception;
            }
        }
    }
}
