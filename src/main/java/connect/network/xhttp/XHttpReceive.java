package connect.network.xhttp;

import connect.network.nio.NioReceive;
import connect.network.xhttp.entity.XHttpResponse;
import util.DirectBufferCleaner;
import util.ReflectionCall;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class XHttpReceive extends NioReceive {

    public XHttpReceive(Object receive, String receiveMethod) {
        super(receive, receiveMethod);
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onRead() throws Exception {
        XHttpResponse response = null;
        try {
            byte[] data = readHttp(channel);
            response = XHttpResponse.parsing(data);
        } catch (Exception e) {
            if (response != null) {
                response.setException(e);
            }
            throw e;
        } finally {
            ReflectionCall.invoke(mReceive, mReceiveMethod, new Class[]{XHttpResponse.class}, new Object[]{response});
        }
    }

    public static byte[] readHttp(SocketChannel channel) throws Exception {
        byte[] data = null;
        ByteBuffer buffer = ByteBuffer.allocate(100);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            boolean isExit = false;
            int isFound = 3;
            do {
                int ret = channel.read(buffer);
                if (ret > 0) {
                    stream.write(buffer.array(), 0, buffer.position());
                    for (int index = 0; index < buffer.position(); index++) {
                        if (buffer.get(index) == 13) {
                            if (buffer.get(index + 1) == 10 && buffer.get(index + 2) == 13 && buffer.get(index + 3) == 10) {

                            }
                        }
                    }
                    if (buffer.position() > 2 && buffer.get(ret - 2) == 13 && buffer.get(ret - 1) == 10) {
                        //找到结束标志
                        isFound--;
                    } else {
                        buffer.clear();
                    }
                } else if (ret == 0) {
                    if (stream.size() == 0) {
                        isExit = true;
                    } else {
                        isExit = isFound == 0;
                    }
                } else {
                    if (isFound == 3) {
                        throw new IOException("SocketChannel read data exception , It could be a disconnection from the server!!!");
                    } else {
                        isExit = true;
                    }
                }
            } while (isExit == false && channel.isConnected());
            if (stream.size() > 0) {
                data = stream.toByteArray();
            }
        } catch (Throwable e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw e;
            }
        }
        DirectBufferCleaner.clean(buffer);
        return data;
    }

    public static byte[] readFullHttp(SocketChannel channel) throws Exception {
        if (channel == null && channel.isConnected()) {
            return null;
        }
        int ret;
        byte[] data = null;
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            boolean isExit = false;
            int isFound = 3;
            do {
                ret = channel.read(buffer);
                if (ret > 0) {
                    stream.write(buffer.array(), 0, buffer.position());
                    if (buffer.position() > 2 && buffer.get(ret - 2) == 13 && buffer.get(ret - 1) == 10) {
                        //找到结束标志
                        isFound--;
                    } else {
                        buffer.clear();
                    }
                } else if (ret == 0) {
                    if (stream.size() == 0) {
                        isExit = true;
                    } else {
                        isExit = isFound == 0;
                    }
                } else {
                    if (isFound == 3) {
                        throw new IOException("SocketChannel read data exception , It could be a disconnection from the server!!!");
                    } else {
                        isExit = true;
                    }
                }
            } while (isExit == false && channel.isConnected());
            if (stream.size() > 0) {
                data = stream.toByteArray();
            }
        } catch (Throwable e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw e;
            }
        }
        DirectBufferCleaner.clean(buffer);
        return data;
    }

}
