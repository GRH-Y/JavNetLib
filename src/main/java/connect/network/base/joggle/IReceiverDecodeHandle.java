package connect.network.base.joggle;

import java.nio.channels.SocketChannel;

public interface IReceiverDecodeHandle {

    void onDecode(SocketChannel channel) throws Throwable;
}
