package connect.network.ssl;

import connect.network.base.joggle.ITLSHandler;
import log.LogDog;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TLSHandler implements ITLSHandler {

    protected SSLEngine sslEngine;
    protected SocketChannel channel;

    private ByteBuffer netSendBuffer;
    private ByteBuffer netReceiveBuffer;
    private ByteBuffer netAppDataBuffer;

    public TLSHandler(SSLEngine engine, SocketChannel channel) {
        this.channel = channel;
        this.sslEngine = engine;
        this.netSendBuffer = newPacketBuffer();
        this.netReceiveBuffer = newPacketBuffer();
        this.netAppDataBuffer = newApplicationBuffer();
//        LogDog.d("==> getPacketBufferSize = " + sslEngine.getSession().getPacketBufferSize());
    }

    protected ByteBuffer getNetSendBuffer() {
        return netSendBuffer;
    }

    protected ByteBuffer getNetReceiveBuffer() {
        return netReceiveBuffer;
    }

    protected ByteBuffer getNetAppDataBuffer() {
        return netAppDataBuffer;
    }

    public ByteBuffer newPacketBuffer() {
        return ByteBuffer.allocate(sslEngine.getSession().getPacketBufferSize());
    }

    public ByteBuffer newApplicationBuffer() {
        return ByteBuffer.allocate(sslEngine.getSession().getApplicationBufferSize());
    }

    public SSLEngine getSslEngine() {
        return sslEngine;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void doHandshake() throws IOException {
//        LogDog.d("==> getApplicationBufferSize = " + sslEngine.getSession().getApplicationBufferSize());
        SSLEngineResult.HandshakeStatus hsStatus = sslEngine.getHandshakeStatus();
        while (hsStatus != SSLEngineResult.HandshakeStatus.FINISHED && hsStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (hsStatus) {
                case NEED_TASK:
//                    LogDog.d("==> doHandshake NEED_TASK !!! ");
                    do {
                        Runnable task = sslEngine.getDelegatedTask();
                        if (task == null) {
                            break;
                        }
                        task.run();
                    } while (true);
                    break;
                case NEED_WRAP:
//                    LogDog.d("==> doHandshake NEED_WRAP !!! ");
                    netSendBuffer = wrapAndWrite(netAppDataBuffer, netSendBuffer);
                    netAppDataBuffer.clear();
                    break;
                case NEED_UNWRAP:
//                    LogDog.d("==> doHandshake NEED_UNWRAP !!! ");
                    readAndUnwrap(null, netAppDataBuffer, true);
                    netAppDataBuffer.clear();
                    break;
            }
            hsStatus = sslEngine.getHandshakeStatus();
        }
    }

    @Override
    public ByteBuffer wrapAndWrite(ByteBuffer needWarp, ByteBuffer result) throws IOException {
        SSLEngineResult.Status status;
        result.clear();
        boolean isNotOk = true;
        do {
            status = sslEngine.wrap(needWarp, result).getStatus();
//            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
//                LogDog.d("==> SSLEngineResult = BUFFER_OVERFLOW ");
//                //SSLEngine无法处理操作，result 缓冲区中没有足够的可用字节来保存结果,则需要创建更大result缓冲区
//                DirectBufferCleaner.clean(result);
//                result = enlargePacketBuffer(sslEngine, result);
//            } else
            if (status == SSLEngineResult.Status.CLOSED) {
                LogDog.d("==> SSLEngineResult = CLOSED ");
                throw new IOException("SSLEngine Closed");
            } else if (status == SSLEngineResult.Status.OK) {
                isNotOk = false;
                flush(result);
            }
        } while (isNotOk);
        return result;
    }

    private void flush(ByteBuffer des) throws IOException {
        des.flip();
        while (des.hasRemaining()) {
            int ret = channel.write(des);
            if (ret < 0) {
                throw new IOException(" SSL Channel is close !!! ");
            }
        }
    }

    @Override
    public void readAndUnwrap(ByteArrayOutputStream result, ByteBuffer unwrap, boolean isDoHandshake) throws IOException {
        SSLEngineResult.Status status;
        boolean isNotOk = true;
        do {
            int ret = channel.read(netReceiveBuffer);
            if (ret < 0) {
                if (result != null) {
                    result.write(unwrap.array(), 0, unwrap.position());
                }
                break;
            }
            netReceiveBuffer.flip();
            status = sslEngine.unwrap(netReceiveBuffer, unwrap).getStatus();
            netReceiveBuffer.compact();

            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
//                LogDog.d("==> SSLEngineResult = BUFFER_OVERFLOW ");
                if (result != null) {
                    result.write(unwrap.array(), 0, unwrap.position());
                }
                unwrap.clear();
            }
//            else if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
//                //没有足够的数据从通道读取需要从套接字读取更多
//                //SSLEngine无法解开传入的数据，因为没有足够的源字节可用来创建完整的数据包。
//                if (!isDoHandshake) {
//                    LogDog.d("==> SSLEngineResult = BUFFER_UNDERFLOW ");
//                }
////                netReceiveBuffer = handleBufferUnderflow(sslEngine, netReceiveBuffer);
////                unwrap.clear();
//            }
            else if (status == SSLEngineResult.Status.CLOSED) {
                LogDog.d("==> SSLEngineResult = CLOSED ");
                throw new IOException("SSLEngine Closed");
            } else if (status == SSLEngineResult.Status.OK && isDoHandshake || netReceiveBuffer.position() == 0) {
                if (result != null) {
                    result.write(unwrap.array(), 0, unwrap.position());
                }
                isNotOk = false;
            }
        } while (isNotOk);
    }

    //-------------------------------------------------------------------------------------------------------------------------

//    protected ByteBuffer enlargePacketBuffer(SSLEngine engine, ByteBuffer buffer) {
//        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
//    }
//
//    protected ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
//        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
//    }
//
//    /**
//     * Compares <code> sessionProposedCapacity <code> with buffer's capacity. If buffer's capacity is smaller,
//     * returns a buffer with the proposed capacity. If it's equal or larger, returns a buffer
//     * with capacity twice the size of the initial one.
//     *
//     * @param buffer                  - the buffer to be enlarged.
//     * @param sessionProposedCapacity - the minimum size of the new buffer
//     * @return A new buffer with a larger capacity.
//     */
//    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
//        if (sessionProposedCapacity > buffer.capacity()) {
//            buffer = ByteBuffer.allocate(sessionProposedCapacity);
//        } else {
//            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
//        }
//        return buffer;
//    }
//
//    /**
//     * Handles {@link SSLEngineResult.Status#BUFFER_UNDERFLOW}. Will check if the buffer is already filled, and if there is no space problem
//     * will return the same buffer, so the client tries to read again. If the buffer is already filled will try to enlarge the buffer either to
//     * session's proposed size or to a larger capacity. A buffer underflow can happen only after an unwrap, so the buffer will always be a
//     * peerNetData buffer.
//     *
//     * @param buffer - will always be peerNetData buffer.
//     * @param engine - the engine used for encryption/decryption of the data exchanged between the two peers.
//     * @return The same buffer if there is no space problem or a new buffer with the same data but more space.
//     */
//    protected ByteBuffer handleBufferUnderflow(SSLEngine engine, ByteBuffer buffer) {
//        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
//            return buffer;
//        } else {
//            ByteBuffer replaceBuffer = enlargePacketBuffer(engine, buffer);
//            buffer.flip();
//            replaceBuffer.put(buffer);
//            return replaceBuffer;
//        }
//    }
}
