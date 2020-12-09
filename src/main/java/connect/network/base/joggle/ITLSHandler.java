package connect.network.base.joggle;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;

public interface ITLSHandler {

    void doHandshake(SocketChannel channel) throws Throwable;

    void doHandshake(AsynchronousSocketChannel channel) throws Exception;

    void wrapAndWrite(SocketChannel channel, ByteBuffer... needWarp) throws Throwable;

    void wrapAndWrite(AsynchronousSocketChannel channel, ByteBuffer needWarp, CompletionHandler<Integer, ByteBuffer> handler) throws Exception;

    int readAndUnwrap(SocketChannel channel, boolean isDoHandshake, ByteBuffer... buffer) throws Throwable;

    int readAndUnwrap(AsynchronousSocketChannel channel, CompletionHandler<Integer, ByteBuffer> handler, ByteBuffer... buffer) throws Exception;
}

