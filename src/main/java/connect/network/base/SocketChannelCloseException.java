package connect.network.base;

import java.net.SocketException;

public class SocketChannelCloseException extends SocketException {

    public SocketChannelCloseException() {
        super("read data return - 1");
    }
}
