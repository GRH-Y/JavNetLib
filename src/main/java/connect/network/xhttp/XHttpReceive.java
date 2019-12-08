package connect.network.xhttp;

import connect.network.nio.NioReceive;
import connect.network.xhttp.entity.XHttpResponse;
import util.DirectBufferCleaner;
import util.ReflectionCall;
import util.StringEnvoy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;

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
        XHttpResponse response = new XHttpResponse();
        try {
            readHttpHead(response, channel);
            readHttpConntent(response, channel);
        } catch (Exception e) {
            response.setException(e);
            throw e;
        } finally {
            ReflectionCall.invoke(mReceive, mReceiveMethod, new Class[]{XHttpResponse.class}, new Object[]{response});
        }
    }

    public void readHttpConntent(XHttpResponse response, SocketChannel channel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            boolean isExit = false;
            do {
                int ret = channel.read(buffer);
                if (ret > 0) {
                    stream.write(buffer.array(), 0, buffer.position());
                    for (int index = 0; index < buffer.position(); index++) {
                        if (buffer.get(index) == 13) {
                            if (buffer.get(index + 1) == 10 && buffer.get(index + 2) == 13 && buffer.get(index + 3) == 10) {
                                //找到http协议分割线
                                int splitIndex = index + 4;
                                byte[] data = stream.toByteArray();
                                String headStr = new String(data, 0, data.length - (buffer.position() - splitIndex));
                                XHttpResponse.parsingHead(response, headStr);
                                String length = response.getHeadForKey(HttpProtocol.XY_CONTENT_LENGTH);
                                if (StringEnvoy.isEmpty(length)) {
                                    byte[] content = new byte[buffer.position() - splitIndex];
                                    String[] array = new String(content).split("\r\n");
                                    System.arraycopy(data, headStr.length(), content, 0, content.length);
                                    stream.reset();
                                    stream.write(content);
                                } else {

                                }
                            }
                        }
                    }
//                    if (buffer.position() > 2 && buffer.get(ret - 2) == 13 && buffer.get(ret - 1) == 10) {
//                        //找到结束标志
//                        isFound--;
//                    } else {
                    buffer.clear();
//                    }
                }
//                else if (ret == 0) {
//                    if (stream.size() == 0) {
//                        isExit = true;
//                    } else {
//                        isExit = isFound == 0;
//                    }
//                } else {
//                    if (isFound == 3) {
//                        throw new IOException("SocketChannel read data exception , It could be a disconnection from the server!!!");
//                    } else {
//                        isExit = true;
//                    }
//                }
            } while (channel.isConnected());
        } catch (Throwable e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw e;
            }
        } finally {
            DirectBufferCleaner.clean(buffer);
        }
    }

    private void readHttpHead(XHttpResponse response, SocketChannel channel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        Map<String, String> headMap = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            int isFound = 4;
            do {
                int ret = channel.read(buffer);
                if (ret > 0) {
                    if (buffer.get(0) == 13) {
                        isFound--;
                    } else if (buffer.get(0) == 10 && (isFound == 3 || isFound == 1)) {
                        isFound--;
                    } else {
                        isFound = 4;
                    }
                    if (isFound == 2) {
                        if (headMap == null) {
                            headMap = new LinkedHashMap<>();
                        }
                        String str = new String(stream.toByteArray(), 0, stream.size() - 1);
                        if (headMap.isEmpty()) {
                            headMap.put(HttpProtocol.XY_RESPONSE_CODE, str);
                        } else {
                            String[] args = str.split(": ");
                            if (args.length == 2) {
                                headMap.put(args[0], args[1]);
                            }
                        }
                        stream.reset();
                    } else if (isFound != 0) {
                        stream.write(buffer.array(), 0, buffer.position());
                    }
                }
                buffer.clear();
            } while (isFound > 0 && channel.isConnected());
            response.setHeadMap(headMap);
        } catch (Throwable e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw e;
            }
        } finally {
            DirectBufferCleaner.clean(buffer);
        }
    }

    public byte[] readFullHttp(SocketChannel channel) throws Exception {
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
