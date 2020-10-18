package connect.network.base.joggle;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;

public interface ITLSHandler {

    void doHandshake(SocketChannel channel) throws IOException;

    void doHandshake(AsynchronousSocketChannel channel) throws Exception;

    void wrapAndWrite(SocketChannel channel, ByteBuffer needWarp) throws IOException;

    void wrapAndWrite(AsynchronousSocketChannel channel, ByteBuffer needWarp, CompletionHandler<Integer, ByteBuffer> handler) throws Exception;

    ByteBuffer readAndUnwrap(SocketChannel channel, boolean isDoHandshake) throws IOException;

    ByteBuffer readAndUnwrap(AsynchronousSocketChannel channel, boolean isDoHandshake) throws Exception;
}

