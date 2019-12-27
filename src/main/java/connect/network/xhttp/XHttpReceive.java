package connect.network.xhttp;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceive;
import connect.network.xhttp.entity.XHttpResponse;
import log.LogDog;
import storage.GZipUtils;
import util.DirectBufferCleaner;
import util.IoEnvoy;
import util.ReflectionCall;
import util.StringEnvoy;

import java.io.ByteArrayOutputStream;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;

public class XHttpReceive extends NioReceive {

    public XHttpReceive(NioClientTask clientTask, Object receive, String receiveMethod) {
        super(clientTask, receive, receiveMethod);
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onRead() throws Exception {
        if (!clientTask.isTaskNeedClose()) {
            XHttpResponse response = new XHttpResponse();
            try {
                readHttpHead(response, channel);
                readHttpContent(response, channel);
            } catch (Exception e) {
                response.setException(e);
                throw e;
            } finally {
                ReflectionCall.invoke(mReceive, mReceiveMethod, new Class[]{XHttpResponse.class}, new Object[]{response});
            }
        }
    }

    /**
     * 读取http协议数据体
     *
     * @param response
     * @param channel
     * @throws Exception
     */
    public void readHttpContent(XHttpResponse response, SocketChannel channel) throws Exception {
        String length = response.getHeadForKey(HttpProtocol.XY_CONTENT_LENGTH);
        String encode = response.getHeadForKey(HttpProtocol.XY_CONTENT_ENCODING);

        if (StringEnvoy.isNotEmpty(length)) {
            int dataSize = Integer.parseInt(length);
            if (dataSize > 0) {
                ByteBuffer buffer = ByteBuffer.allocate(dataSize);
                try {
                    int ret = IoEnvoy.readToFull(channel, buffer);
                    if (ret != IoEnvoy.FAIL) {
                        if (StringEnvoy.isNotEmpty(encode) && encode.contains("gzip")) {
                            //需要解压
                            response.setRaw(GZipUtils.unCompress(buffer.array()));
                        } else {
                            //不需要任何处理，直接保存
                            response.setRaw(buffer.array());
                        }
                    }
                } catch (Exception e) {
                    throw e;
                } finally {
                    DirectBufferCleaner.clean(buffer);
                }
            }
        } else {
            ByteBuffer buffer = ByteBuffer.allocate(1);
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
                 ByteArrayOutputStream slicesStream = new ByteArrayOutputStream()) {
                boolean isExit = false;
                int isFound = 2;
                boolean isSlicesData = true;
                do {
                    int ret = channel.read(buffer);
                    if (ret > 0) {
                        stream.write(buffer.array(), 0, buffer.position());
                        if (buffer.get(0) == 13) {
                            isFound--;
                        } else if (buffer.get(0) == 10 && isFound == 1) {
                            if (isSlicesData) {
                                int slicesDataSize = Integer.parseInt(new String(stream.toByteArray(), 0, stream.size() - 2), 16);
                                if (slicesDataSize > 0) {
                                    ByteBuffer slicesBuffer = ByteBuffer.allocate(slicesDataSize);
                                    ret = IoEnvoy.readToFull(channel, slicesBuffer);
                                    if (ret != IoEnvoy.FAIL) {
                                        slicesStream.write(slicesBuffer.array());
                                        DirectBufferCleaner.clean(slicesBuffer);
                                    }
                                } else if (slicesDataSize == 0) {
                                    //保存数据
                                    if (slicesStream.size() > 0) {
                                        if (StringEnvoy.isNotEmpty(encode) && encode.contains("gzip")) {
                                            //需要解压
                                            response.setRaw(GZipUtils.unCompress(slicesStream.toByteArray()));
                                        } else {
                                            //不需要任何处理，直接保存
                                            response.setRaw(slicesStream.toByteArray());
                                        }
                                    }
                                    isSlicesData = false;
                                }
                                //skip 2 byte \r\n
                                buffer.clear();
                                channel.read(buffer);
                                buffer.clear();
                                channel.read(buffer);
                                stream.reset();
                                isFound = 2;
                            } else {
                                response.setData(new String(stream.toByteArray()));
                                isExit = true;
                            }
                        } else {
                            isFound = 2;
                        }
                        buffer.clear();
                    } else if (ret == 0) {
                        isExit = isSlicesData == false;
                    } else {
                        isExit = true;
                    }
                } while (isExit == false && channel.isConnected());

            } catch (Throwable e) {
                if (!(e instanceof SocketTimeoutException)) {
                    throw e;
                }
            } finally {
                DirectBufferCleaner.clean(buffer);
            }
        }
    }

    /**
     * 读取http协议头部数据
     *
     * @param response
     * @param channel
     * @throws Exception
     */
    private void readHttpHead(XHttpResponse response, SocketChannel channel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        Map<String, String> headMap = null;
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            int isFound = 2;
            boolean isExit = false;
            do {
                int ret = channel.read(buffer);
                if (ret > 0) {
                    stream.write(buffer.array(), 0, buffer.position());
                    if (buffer.get(0) == 13) {
                        isFound--;
                    } else if (buffer.get(0) == 10 && isFound == 1) {
                        if (headMap == null) {
                            headMap = new LinkedHashMap<>();
                        }
                        if (stream.size() == 2) {
                            isExit = true;
                        } else {
                            String str = new String(stream.toByteArray(), 0, stream.size() - 2);
                            if (headMap.isEmpty()) {
                                headMap.put(HttpProtocol.XY_RESPONSE_CODE, str);
                            } else {
                                String[] args = str.split(": ");
                                if (args.length == 2) {
                                    headMap.put(args[0], args[1]);
                                }
                            }
                        }
                        stream.reset();
                        isFound = 2;
                    } else {
                        isFound = 2;
                    }
                } else if (ret < 0) {
                    isExit = true;
                }
                buffer.clear();
            } while (isExit == false && channel.isConnected());
            response.setHeadMap(headMap);
            if (headMap != null) {
                for (Map.Entry<String, String> entry : headMap.entrySet()) {
                    LogDog.d("==> " + entry.getKey() + ":" + entry.getValue());
                }
            }
        } catch (Throwable e) {
            if (!(e instanceof SocketTimeoutException)) {
                throw e;
            }
        } finally {
            DirectBufferCleaner.clean(buffer);
        }
    }
}