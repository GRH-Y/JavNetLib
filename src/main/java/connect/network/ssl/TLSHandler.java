package connect.network.ssl;

import connect.network.base.SocketChannelCloseException;
import connect.network.base.joggle.ITLSHandler;
import connect.network.xhttp.utils.MultiLevelBuf;
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

    protected MultiLevelBuf sendBuf;

    public static final int NOT_ENOUGH_CAPACITY = 3;
    public static final int OK = 0;

    public TLSHandler(SSLEngine engine) {
        if (engine == null) {
            throw new NullPointerException("engine is null !!! ");
        }
        this.sslEngine = engine;
        this.sendBuffer = newPacketBuffer();
        this.receiveBuffer = newPacketBuffer();
        sendBuf = new MultiLevelBuf();
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
        SSLEngineResult.HandshakeStatus status = sslEngine.getHandshakeStatus();
        while (status != SSLEngineResult.HandshakeStatus.FINISHED && status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (status) {
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
            status = sslEngine.getHandshakeStatus();
        }
        receiveBuffer.clear();
    }

    @Override
    public void doHandshake(AsynchronousSocketChannel channel) throws Exception {
        SSLEngineResult.HandshakeStatus status = sslEngine.getHandshakeStatus();
        while (status != SSLEngineResult.HandshakeStatus.FINISHED && status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (status) {
                case NEED_TASK:
                    LogDog.d("doHandshake NEED_TASK");
                    do {
                        Runnable task = sslEngine.getDelegatedTask();
                        if (task == null) {
                            break;
                        }
                        task.run();
                    } while (true);
                    break;
                case NEED_WRAP:
                    LogDog.d("doHandshake NEED_WRAP");
                    wrapAndWrite(channel, appDataBuffer, null);
                    break;
                case NEED_UNWRAP:
                    LogDog.d("doHandshake NEED_UNWRAP");
                    readAndUnwrap(channel, null, appDataBuffer);
                    break;
            }
            status = sslEngine.getHandshakeStatus();
        }
        receiveBuffer.clear();
    }

    @Override
    public void wrapAndWrite(SocketChannel channel, ByteBuffer... warp) throws Throwable {
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
                    LogDog.d("==> wrapAndWrite BUFFER_OVERFLOW !!! ");
                    ByteBuffer newBuffer = newApplicationBuffer();
                    sendBuffer.flip();
                    newBuffer.put(sendBuffer);
                    sendBuffer = newBuffer;
                    break;
                case OK:
                    sendBuffer.flip();
                    if (handler == null) {
                        while (sendBuffer.hasRemaining()) {
                            Future<Integer> future = channel.write(sendBuffer);
                            Integer ret = future.get();
                            if (ret.intValue() < 0) {
                                throw new SocketChannelCloseException();
                            }
                        }
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
    public int readAndUnwrap(AsynchronousSocketChannel channel, CompletionHandler<Integer, ByteBuffer> handler, ByteBuffer... buffer) throws Exception {
        do {
            if (handler != null) {
                channel.read(receiveBuffer, receiveBuffer, handler);
            } else {
                Future<Integer> future = channel.read(receiveBuffer);
                Integer ret = future.get();
                if (ret.intValue() < 0) {
                    throw new SocketChannelCloseException();
                }
            }
            receiveBuffer.flip();
            SSLEngineResult.Status status = sslEngine.unwrap(receiveBuffer, buffer).getStatus();
            receiveBuffer.compact();
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    //SSLEngine 不能进行该操作，因为在目标缓冲区没有足够的字节空间可以容纳结果。
                    LogDog.d("==> readAndUnwrap BUFFER_OVERFLOW !!! ");
//                    ByteBuffer newBuffer = newApplicationBuffer();
//                    appDataBuffer.flip();
//                    newBuffer.put(appDataBuffer);
//                    appDataBuffer = newBuffer;
                    return NOT_ENOUGH_CAPACITY;
                case OK:
                    return OK;
//                    break;
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
                if (sslEngine.isOutboundDone()) {
                    sslEngine.closeOutbound();
                }
                if (sslEngine.isInboundDone()) {
                    sslEngine.closeInbound();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------

}
