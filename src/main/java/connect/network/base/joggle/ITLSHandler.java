package connect.network.base.joggle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface ITLSHandler {

    void doHandshake(SocketChannel channel) throws IOException;

    void wrapAndWrite(SocketChannel channel, ByteBuffer needWarp) throws IOException;

    ByteBuffer readAndUnwrap(SocketChannel channel, boolean isDoHandshake) throws IOException;
}

