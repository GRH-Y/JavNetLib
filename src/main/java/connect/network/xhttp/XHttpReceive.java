package connect.network.xhttp;

import connect.network.base.joggle.INetReceive;
import connect.network.nio.NioReceive;
import connect.network.xhttp.entity.XResponse;
import connect.network.xhttp.entity.XResponseHelper;
import log.LogDog;
import util.DirectBufferCleaner;
import util.IoEnvoy;
import util.StringEnvoy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;

public class XHttpReceive extends NioReceive<XResponse> {

    protected XResponse response;

    public XHttpReceive(INetReceive receive) {
        super(receive);
        response = new XResponse();
        LinkedHashMap headMap = new LinkedHashMap<>();
        response.setHttpHead(headMap);
    }

    public void reset() {
        response.reset();
    }


    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    @Override
    protected void onRead(SocketChannel channel) throws Exception {
        Exception exception = null;
        try {
            readHttpFullData(channel);
            onResponse(response);
        } catch (Exception e) {
            exception = e;
            throw exception;
        } finally {
            notifyReceiver(response, exception);
        }
    }


    /**
     * 读取http协议头部数据
     *
     * @throws Exception
     */
    protected void readHttpFullData(SocketChannel channel) throws Exception {
        int isFound = 2;
        boolean isExit = false;
        boolean isHeadMode = true;
        ByteBuffer buffer = ByteBuffer.allocate(1);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             ByteArrayOutputStream slicesStream = new ByteArrayOutputStream()) {
            do {
                buffer.clear();
                int ret = channel.read(buffer);
                if (ret < 0) {
                    throw new IOException("http channel is close !!!");
                }
                if (ret == 0 && stream.size() == 3) {
                    throw new IOException("\\u0005\\u0001\\u0000 invalid data !!!");
                }
                stream.write(buffer.array(), 0, buffer.position());
                if (buffer.get(0) != '\r' && buffer.get(0) != '\n') {
                    isFound = 2;
                    continue;
                }
                if (buffer.get(0) == '\r') {
                    isFound--;
                    continue;
                }
                if (buffer.get(0) == '\n' && isFound == 1) {
                    if (stream.size() == 2) {
                        if (isHeadMode) {
                            //读取完http的head数据
                            isHeadMode = false;
                            int code = XResponseHelper.getCode(response);
                            String requestMethod = XResponseHelper.getRequestMethod(response);
                            if (code == 0 && !"POST".equals(requestMethod)) {
                                //如果code == 0说明读取的是请求类型的response是没有body数据则退出
                                isExit = true;
                                continue;
                            }
                            String length = response.getHeadForKey(XHttpProtocol.XY_CONTENT_LENGTH);
                            //如果有明确的body数据长度则读取完整并退出，没有则解析下一行获取长度
                            if (StringEnvoy.isNotEmpty(length)) {
                                int dataSize = Integer.parseInt(length);
                                if (dataSize > 0) {
                                    readFullBody(dataSize, channel);
                                }
                                isExit = true;
                            }
                        } else {
                            //读取完所有数据
                            isExit = true;
                        }
                    } else {
                        if (isHeadMode) {
                            readHeadMode(stream);
                        } else {
                            readHttpSubsectionBody(stream, slicesStream, channel, buffer);
                        }
                    }
                    stream.reset();
                    isFound = 2;
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


    private void readHeadMode(ByteArrayOutputStream stream) {
        String headLine = new String(stream.toByteArray(), 0, stream.size() - 2);
        Map<String, String> headMap = response.getHttpHead();
        if (headMap.isEmpty()) {
            headMap.put(XHttpProtocol.XY_FIST_LINE, headLine);
        } else {
            String[] args = headLine.split(": ");
            if (args.length == 2) {
                headMap.put(args[0], args[1]);
            }
        }
    }

    private void readHttpSubsectionBody(ByteArrayOutputStream stream, ByteArrayOutputStream slicesStream,
                                        SocketChannel channel, ByteBuffer buffer) throws IOException {
        int slicesDataSize = Integer.parseInt(new String(stream.toByteArray(), 0, stream.size() - 2), 16);
        if (slicesDataSize > 0) {
            ByteBuffer slicesBuffer = null;
            try {
                slicesBuffer = ByteBuffer.allocate(slicesDataSize);
                int ret = IoEnvoy.readToFull(channel, slicesBuffer);
                if (ret != IoEnvoy.FAIL) {
                    slicesStream.write(slicesBuffer.array());
                }
                //skip 3 byte \r\n\n
                skip(channel, buffer, 3);
            } catch (Exception e) {
                throw new IOException("http channel is close !!!");
            } finally {
                DirectBufferCleaner.clean(slicesBuffer);
            }
        } else if (slicesDataSize == 0) {
            //保存数据
            if (slicesStream.size() > 0) {
                response.setHttpData(slicesStream.toByteArray());
            }
        }
    }

    private void skip(SocketChannel channel, ByteBuffer buffer, int skipCount) throws IOException {
        while (skipCount > 0) {
            int ret = channel.read(buffer);
            if (ret < 0) {
                throw new IOException("http channel is close !!!");
            }
            buffer.clear();
            skipCount--;
        }
    }

    private void readFullBody(int dataSize, SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(dataSize);
        try {
            int ret = IoEnvoy.readToFull(channel, buffer);
            if (ret != IoEnvoy.FAIL) {
                response.setHttpData(buffer.array());
            } else {
                throw new IOException("http channel is close !!!");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DirectBufferCleaner.clean(buffer);
        }
    }

    protected void printHeadMap() {
        Map<String, String> headMap = response.getHttpHead();
        if (headMap != null) {
            LogDog.d("============= http response head start ==========> ");
            for (Map.Entry<String, String> entry : headMap.entrySet()) {
                LogDog.d("==> " + entry.getKey() + ":" + entry.getValue());
            }
            LogDog.d("============= http response head end ============> \n");
        }
    }
}
