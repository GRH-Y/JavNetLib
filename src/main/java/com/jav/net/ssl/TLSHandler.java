package com.jav.net.ssl;

import com.jav.common.log.LogDog;
import com.jav.common.util.IoEnvoy;
import com.jav.common.util.SpeedReflex;
import com.jav.net.base.MultiBuffer;
import com.jav.net.base.SocketChannelCloseException;
import com.jav.net.base.joggle.ITLSHandler;

import javax.net.ssl.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;

public class TLSHandler implements ITLSHandler {

    protected SSLEngine mSSLEngine;

    protected ByteBuffer mSendBuffer;
    protected ByteBuffer mReceiveBuffer;
    protected ByteBuffer mAppDataBuffer;

    protected MultiBuffer mSendBuf;

    public static final int NOT_ENOUGH_CAPACITY = 3;
    public static final int OK = 0;

    public static TLSHandler createSSLEngineForClient(SSLContext sslContext, String host, int port) {
        SSLEngine sslEngine = sslContext.createSSLEngine(host, port);
        return new TLSHandler(sslEngine);
    }

    public static TLSHandler createSSLEngineForServer(SSLContext sslContext) {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        return new TLSHandler(sslEngine);
    }

    public TLSHandler(SSLEngine engine) {
        if (engine == null) {
            throw new NullPointerException("engine is null !!! ");
        }
        this.mSSLEngine = engine;
        this.mSendBuffer = newPacketBuffer();
        this.mReceiveBuffer = newPacketBuffer();
        mSendBuf = new MultiBuffer();
        this.mAppDataBuffer = ByteBuffer.allocateDirect(mSSLEngine.getSession().getApplicationBufferSize());
    }

    public void beginHandshakeForClient() throws SSLException {
        mSSLEngine.setUseClientMode(true);
        mSSLEngine.setEnableSessionCreation(true);
        mSSLEngine.beginHandshake();
    }

    public void beginHandshakeForServer() {
        mSSLEngine.setUseClientMode(false);
        mSSLEngine.setEnableSessionCreation(true);
    }

    public SSLEngine getSslEngine() {
        return mSSLEngine;
    }

    private ByteBuffer newPacketBuffer() {
        return ByteBuffer.allocateDirect(mSSLEngine.getSession().getPacketBufferSize());
    }

    private ByteBuffer newApplicationBuffer() {
        return ByteBuffer.allocateDirect(mSSLEngine.getSession().getApplicationBufferSize() + mSSLEngine.getSession().getApplicationBufferSize());
    }

    @Override
    public void doHandshake(SocketChannel channel) throws Throwable {
        SSLEngineResult.HandshakeStatus status = mSSLEngine.getHandshakeStatus();
        while (status != SSLEngineResult.HandshakeStatus.FINISHED && status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (status) {
                case NEED_TASK:
                    do {
                        Runnable task = mSSLEngine.getDelegatedTask();
                        if (task == null) {
                            break;
                        }
                        task.run();
                    } while (true);
                    break;
                case NEED_WRAP:
//                    LogDog.d("doHandshake NEED_WRAP");
                    wrapAndWrite(channel, mAppDataBuffer);
                    break;
                case NEED_UNWRAP:
//                    LogDog.d("doHandshake NEED_UNWRAP");
                    readAndUnwrap(channel, true, mAppDataBuffer);
                    break;
            }
            status = mSSLEngine.getHandshakeStatus();
        }
        mReceiveBuffer.clear();
    }

    @Override
    public void doHandshake(AsynchronousSocketChannel channel) throws Exception {
        SSLEngineResult.HandshakeStatus status = mSSLEngine.getHandshakeStatus();
        while (status != SSLEngineResult.HandshakeStatus.FINISHED && status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (status) {
                case NEED_TASK:
                    LogDog.d("doHandshake NEED_TASK");
                    do {
                        Runnable task = mSSLEngine.getDelegatedTask();
                        if (task == null) {
                            break;
                        }
                        task.run();
                    } while (true);
                    break;
                case NEED_WRAP:
                    LogDog.d("doHandshake NEED_WRAP");
                    wrapAndWrite(channel, mAppDataBuffer, null);
                    break;
                case NEED_UNWRAP:
                    LogDog.d("doHandshake NEED_UNWRAP");
                    readAndUnwrap(channel, null, mAppDataBuffer);
                    break;
            }
            status = mSSLEngine.getHandshakeStatus();
        }
        mReceiveBuffer.clear();
    }

    @Override
    public void wrapAndWrite(SocketChannel channel, ByteBuffer... warp) throws Throwable {
        do {
            SSLEngineResult.Status status = mSSLEngine.wrap(warp, mSendBuffer).getStatus();
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    LogDog.d("==> wrapAndWrite BUFFER_OVERFLOW !!! ");
                    ByteBuffer newBuffer = newApplicationBuffer();
                    mSendBuffer.flip();
                    newBuffer.put(mSendBuffer);
                    mSendBuffer = newBuffer;
                    break;
                case OK:
                    mSendBuffer.flip();
                    IoEnvoy.writeToFull(channel, mSendBuffer);
                    mSendBuffer.clear();
                    return;
            }
        } while (true);
    }

    @Override
    public void wrapAndWrite(AsynchronousSocketChannel channel, ByteBuffer warp, CompletionHandler<Integer, ByteBuffer> handler) throws Exception {
        do {
            SSLEngineResult.Status status = mSSLEngine.wrap(warp, mSendBuffer).getStatus();
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    LogDog.d("==> wrapAndWrite BUFFER_OVERFLOW !!! ");
                    ByteBuffer newBuffer = newApplicationBuffer();
                    mSendBuffer.flip();
                    newBuffer.put(mSendBuffer);
                    mSendBuffer = newBuffer;
                    break;
                case OK:
                    mSendBuffer.flip();
                    if (handler == null) {
                        while (mSendBuffer.hasRemaining()) {
                            Future<Integer> future = channel.write(mSendBuffer);
                            Integer ret = future.get();
                            if (ret < 0) {
                                throw new SocketChannelCloseException();
                            }
                        }
                        mSendBuffer.clear();
                    } else {
                        channel.write(mSendBuffer, mSendBuffer, handler);
                    }
                    return;
            }
        } while (true);
    }

    @Override
    public int readAndUnwrap(SocketChannel channel, boolean isDoHandshake, ByteBuffer... buffer) throws Throwable {
        do {
            int ret = channel.read(mReceiveBuffer);
            if (ret < 0) {
                throw new SocketChannelCloseException();
            }
            mReceiveBuffer.flip();
            SSLEngineResult.Status status = mSSLEngine.unwrap(mReceiveBuffer, buffer).getStatus();
            mReceiveBuffer.compact();
            //不能对传入的数据解包，因为没有足够的源字节可以用来生成一个完整的包。
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    //SSLEngine 不能进行该操作，因为在目标缓冲区没有足够的字节空间可以容纳结果。
                    if (isDoHandshake) {
                        LogDog.d("==> readAndUnwrap BUFFER_OVERFLOW !!! ");
                        ByteBuffer newBuffer = newApplicationBuffer();
                        mAppDataBuffer.flip();
                        newBuffer.put(mAppDataBuffer);
                        mAppDataBuffer = newBuffer;
                        buffer[0] = mAppDataBuffer;
                    }
                    return NOT_ENOUGH_CAPACITY;
                case OK:
                    if (isDoHandshake || mReceiveBuffer.position() == 0) {
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
                channel.read(mReceiveBuffer, mReceiveBuffer, handler);
            } else {
                Future<Integer> future = channel.read(mReceiveBuffer);
                Integer ret = future.get();
                if (ret < 0) {
                    throw new SocketChannelCloseException();
                }
            }
            mReceiveBuffer.flip();
            SSLEngineResult.Status status = mSSLEngine.unwrap(mReceiveBuffer, buffer).getStatus();
            mReceiveBuffer.compact();
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
        if (mSSLEngine != null) {
            try {
                SSLSession handSession = mSSLEngine.getHandshakeSession();
                if (handSession != null) {
                    handSession.invalidate();
                }
                SSLSession sslSession = mSSLEngine.getSession();
                if (sslSession != null) {
                    sslSession.invalidate();
                }
                try {
                    if (mSSLEngine.isInboundDone()) {
                        mSSLEngine.closeInbound();
                    }
                } catch (Exception e1) {
                    Field field = SpeedReflex.getInstance().getField(mSSLEngine.getClass(), "conContext");
                    field.setAccessible(true);
                    Object context = field.get(mSSLEngine);
                    Method method = SpeedReflex.getInstance().getMethod(context.getClass(), "closeInbound");
                    method.setAccessible(true);
                    method.invoke(context);
                }
                try {
                    mSSLEngine.closeOutbound();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------

}
