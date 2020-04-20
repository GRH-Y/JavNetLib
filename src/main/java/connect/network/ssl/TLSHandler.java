package connect.network.ssl;

import connect.network.base.joggle.ITLSHandler;
import util.DirectBufferCleaner;
import util.IoEnvoy;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TLSHandler implements ITLSHandler {

    protected SSLEngine sslEngine;

    protected ByteBuffer sendBuffer;
    protected ByteBuffer receiveBuffer;
    protected ByteBuffer appDataBuffer;

    public TLSHandler(SSLEngine engine) {
        this.sslEngine = engine;
        this.sendBuffer = newPacketBuffer();
        this.receiveBuffer = newPacketBuffer();
        this.appDataBuffer = newApplicationBuffer(0);
    }

    private ByteBuffer newPacketBuffer() {
        return ByteBuffer.allocateDirect(sslEngine.getSession().getPacketBufferSize());
    }

    private ByteBuffer newApplicationBuffer(int exSize) {
        return ByteBuffer.allocateDirect(sslEngine.getSession().getApplicationBufferSize() + exSize);
    }

    @Override
    public void doHandshake(SocketChannel channel) throws IOException {
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
                    readAndUnwrap(channel, true);
                    break;
            }
            hsStatus = sslEngine.getHandshakeStatus();
        }
        receiveBuffer.clear();
    }

    @Override
    public void wrapAndWrite(SocketChannel channel, ByteBuffer warp) throws IOException {
        do {
            SSLEngineResult.Status status = sslEngine.wrap(warp, sendBuffer).getStatus();
            switch (status) {
                case CLOSED:
                    throw new IOException("SSLEngine Closed");
                case BUFFER_OVERFLOW:
                    ByteBuffer newBuffer = newApplicationBuffer(sendBuffer.position());
                    sendBuffer.flip();
                    newBuffer.put(sendBuffer);
                    DirectBufferCleaner.clean(sendBuffer);
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
    public ByteBuffer readAndUnwrap(SocketChannel channel, boolean isDoHandshake) throws IOException {
        do {
            int ret = channel.read(receiveBuffer);
            if (ret < 0) {
                throw new IOException("SocketChannel close !!!");
            }
//            else if (ret == 0 && receiveBuffer.position() == 0) {
//                return appDataBuffer;
//            }
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
                    ByteBuffer newBuffer = newApplicationBuffer(appDataBuffer.position());
                    appDataBuffer.flip();
                    newBuffer.put(appDataBuffer);
                    DirectBufferCleaner.clean(appDataBuffer);
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
        DirectBufferCleaner.clean(sendBuffer);
        DirectBufferCleaner.clean(receiveBuffer);
        DirectBufferCleaner.clean(appDataBuffer);
        sslEngine = null;
    }

    //-------------------------------------------------------------------------------------------------------------------------

}
