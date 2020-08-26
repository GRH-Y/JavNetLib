package connect.network.tcp;


import connect.network.base.joggle.INetReceiver;
import util.IoEnvoy;

import java.io.InputStream;

public class TcpReceive {

    protected INetReceiver receive;
    protected InputStream stream = null;

    public TcpReceive(INetReceiver receive) {
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
            throw e;
        } finally {
            notifyReceiver(data, exception);
        }
    }

    protected void notifyReceiver(Object data, Exception exception) {
        if (receive != null) {
            receive.onReceiveFullData(data);
            if (exception != null) {
                receive.onReceiveException(exception);
            }
        }
    }
}
