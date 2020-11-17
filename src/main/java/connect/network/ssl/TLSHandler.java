package connect.network.ssl;

import connect.network.base.SocketChannelCloseException;
import connect.network.base.joggle.ITLSHandler;
import log.LogDog;
import util.IoEnvoy;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

public class TLSHandler implements ITLSHandler {

    protected SSLEngine sslEngine;

    protected ByteBuffer sendBuffer;
    protected ByteBuffer receiveBuffer;
    protected ByteBuffer appDataBuffer;

    public static final int NOT_ENOUGH_CAPACITY = 3;
    public static final int OK = 0;

    public TLSHandler(SSLEngine engine) {
        if (engine == null) {
            throw new NullPointerException("engine is null !!! ");
        }
        this.sslEngine = engine;
        this.sendBuffer = newPacketBuffer();
        this.receiveBuffer = newPacketBuffer();
        this.appDataBuffer = ByteBuffer.allocateDirect(sslEngine.getSession().getApplicationBufferSize());
    }

    public SSLEngine getSslEngine() {
        return sslEngine;
    }

    private ByteBuffer newPacketBuffer() {
        return ByteBuffer.allocateDirect(sslEngine.getSession().getPacketBufferSize());
    }

    private ByteBuffer newApplicationBuffer() {
        return ByteBuffer.allocateDirect(sslEngine.getSession().getApplicationBufferSize() + sslEngine.getSession().getApplicationBufferSize());
    }

    @Override
    public void doHandshake(SocketChannel channel) throws Throwable {
        SSLEngineResult.HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
        while (hsStatus != SSLEngineResult.HandshakeStatus.FINISHED && hsStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (hsStatus) {
                case NEED_TASK:
                    do {
                        Runnable task = sslEngine.getDelegatedTask();
                        if (task == null) {
                            break;
                        }
                        task.run();
                    } while (true);
                    break;
                case NEED_WRAP:
//                    LogDog.d("doHandshake NEED_WRAP");
                    wrapAndWrite(channel, appDataBuffer);
                    break;
                case NEED_UNWRAP:
//                    LogDog.d("doHandshake NEED_UNWRAP");
                    readAndUnwrap(channel, true, appDataBuffer);
                    break;
            }
            hsStatus = sslEngine.getHandshakeStatus();
        }
        receiveBuffer.clear();
    }

    @Override
    public void doHandshake(AsynchronousSocketChannel channel) throws Exception {
        SSLEngineResult.HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
        while (hsStatus != SSLEngineResult.HandshakeStatus.FINISHED && hsStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (hsStatus) {
                case NEED_TASK:
                    do {
                        Runnable task = sslEngine.getDelegatedTask();
                        if (task == null) {
                            break;
                        }
                        task.run();
                    } while (true);
                    break;
                case NEED_WRAP:
//                    LogDog.d("doHandshake NEED_WRAP");
                    wrapAndWrite(channel, appDataBuffer, null);
                    break;
                case NEED_UNWRAP:
//                    LogDog.d("doHandshake NEED_UNWRAP");
                    readAndUnwrap(channel, true);
                    break;
            }
            hsStatus = sslEngine.getHandshakeStatus();
        }
        receiveBuffer.clear();
    }

    @Override
    public void wrapAndWrite(SocketChannel channel, ByteBuffer warp) throws Throwable {
        do {
            SSLEngineResult.Status status = sslEngine.wrap(warp, sendBuffer).getStatus();
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    LogDog.d("==> wrapAndWrite BUFFER_OVERFLOW !!! ");
                    ByteBuffer newBuffer = newApplicationBuffer();
                    sendBuffer.flip();
                    newBuffer.put(sendBuffer);
                    sendBuffer = newBuffer;
                    break;
                case OK:
                    sendBuffer.flip();
                    IoEnvoy.writeToFull(channel, sendBuffer);
                    sendBuffer.clear();
                    return;
            }
        } while (true);
    }

    @Override
    public void wrapAndWrite(AsynchronousSocketChannel channel, ByteBuffer warp, CompletionHandler<Integer, ByteBuffer> handler) throws Exception {
        do {
            SSLEngineResult.Status status = sslEngine.wrap(warp, sendBuffer).getStatus();
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    ByteBuffer newBuffer = newApplicationBuffer();
                    sendBuffer.flip();
                    newBuffer.put(sendBuffer);
                    sendBuffer = newBuffer;
                    break;
                case OK:
                    sendBuffer.flip();
                    if (handler == null) {
                        Future<Integer> future = channel.write(sendBuffer);
                        future.get();
                        sendBuffer.clear();
                    } else {
                        channel.write(sendBuffer, sendBuffer, handler);
                    }
                    return;
            }
        } while (true);
    }

    @Override
    public int readAndUnwrap(SocketChannel channel, boolean isDoHandshake, ByteBuffer... buffer) throws Throwable {
        do {
            int ret = channel.read(receiveBuffer);
            if (ret < 0) {
                throw new SocketChannelCloseException();
            }
            receiveBuffer.flip();
            SSLEngineResult.Status status = sslEngine.unwrap(receiveBuffer, buffer).getStatus();
            receiveBuffer.compact();
            //不能对传入的数据解包，因为没有足够的源字节可以用来生成一个完整的包。
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    //SSLEngine 不能进行该操作，因为在目标缓冲区没有足够的字节空间可以容纳结果。
                    if (isDoHandshake) {
                        LogDog.d("==> readAndUnwrap BUFFER_OVERFLOW !!! ");
                        ByteBuffer newBuffer = newApplicationBuffer();
                        appDataBuffer.flip();
                        newBuffer.put(appDataBuffer);
                        appDataBuffer = newBuffer;
                        buffer[0] = appDataBuffer;
                    }
                    return NOT_ENOUGH_CAPACITY;
                case OK:
                    if (isDoHandshake || receiveBuffer.position() == 0) {
                        return OK;
                    }
                    break;
            }
        } while (true);
    }

    @Override
    public ByteBuffer readAndUnwrap(AsynchronousSocketChannel channel, boolean isDoHandshake) throws Exception {
        do {
            Future<Integer> future = channel.read(receiveBuffer);
            future.get();
            receiveBuffer.flip();
            SSLEngineResult.Status status = sslEngine.unwrap(receiveBuffer, appDataBuffer).getStatus();
            receiveBuffer.compact();
            // case BUFFER_UNDERFLOW:
            //不能对传入的数据解包，因为没有足够的源字节可以用来生成一个完整的包。
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    //SSLEngine 不能进行该操作，因为在目标缓冲区没有足够的字节空间可以容纳结果。
//                    LogDog.d("==> readAndUnwrap BUFFER_OVERFLOW !!! ");
                    ByteBuffer newBuffer = newApplicationBuffer();
                    appDataBuffer.flip();
                    newBuffer.put(appDataBuffer);
                    appDataBuffer = newBuffer;
                    break;
                case OK:
                    if (isDoHandshake || receiveBuffer.position() == 0) {
                        return appDataBuffer;
                    }
                    break;
            }
        } while (true);
    }

    public void release() {
        if (sslEngine != null) {
            try {
                SSLSession handSession = sslEngine.getHandshakeSession();
                if (handSession != null) {
                    handSession.invalidate();
                }
                SSLSession sslSession = sslEngine.getSession();
                if (sslSession != null) {
                    sslSession.invalidate();
                }
                sslEngine.closeOutbound();
//                    sslEngine.closeInbound();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sslEngine = null;
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------

}
