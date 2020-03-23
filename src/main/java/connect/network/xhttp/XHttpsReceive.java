package connect.network.xhttp;

import connect.network.base.joggle.INetReceive;
import connect.network.ssl.TLSHandler;
import log.LogDog;

import javax.net.ssl.SSLEngine;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class XHttpsReceive extends XHttpReceive {

    protected TLSHandler tlsHandler;
    protected SSLEngine sslEngine;
    protected ByteBuffer receiveBuffer;
    protected ByteArrayOutputStream result;

    public XHttpsReceive(TLSHandler tlsHandler, INetReceive receive) {
        super(receive);
        if (tlsHandler == null) {
            throw new IllegalArgumentException("tlsHandler is null !!!");
        }
        this.tlsHandler = tlsHandler;
        this.sslEngine = tlsHandler.getSslEngine();
        this.receiveBuffer = tlsHandler.newApplicationBuffer();
        this.result = new ByteArrayOutputStream();
    }

    @Override
    protected void onRead(SocketChannel channel) throws Exception {
        result.reset();
        receiveBuffer.clear();
        Exception exception = null;
        try {
            boolean isFull = false;
            byte[] decodeData = null;
            while (!isFull) {
                tlsHandler.readAndUnwrap(result, receiveBuffer, false);
                decodeData = result.toByteArray();
                //判断协议头如果含有 Transfer-Encoding 字段，则需要判断数据末尾2为是否是 \r\n
                isFull = !result.toString().contains(XHttpProtocol.XY_TRANSFER_ENCODING)
                        || (decodeData[decodeData.length - 2] == '\r'
                        && decodeData[decodeData.length - 1] == '\n');
            }
            //查找协议头跟body的分隔符 \r\n\r\n
            int index = findHedaSplitIndex(decodeData);
            if (index == -1) {
                return;
            }
            //解析协议头
            analysisHttpHead(new String(decodeData, 0, index));

            int bodySize = receiveBuffer.position() - index - 4;
            if (bodySize > 0) {
                byte[] httpBody = new byte[bodySize];
                System.arraycopy(decodeData, index + 4, httpBody, 0, bodySize);
                response.setHttpData(httpBody);
            }
            onResponse(response);
        } catch (Exception e) {
            exception = e;
            throw exception;
        } finally {
            notifyReceiver(response, exception);
        }
    }


    private int findHedaSplitIndex(byte[] data) {
        int index = -1;
        for (int pos = 0; pos < data.length; pos++) {
            if (data[pos] == '\r') {
                if (data[pos + 1] == '\n' && data[pos + 2] == '\r' && data[pos + 3] == '\n') {
                    index = pos;
                    break;
                }
            }
        }
        return index;
    }

    private void analysisHttpHead(String headStr) {
        String[] heads = headStr.split("\r\n");
        //解析http head
        Map<String, String> headMap = response.getHttpHead();
        if (headMap == null) {
            headMap = new HashMap<>();
            response.setHttpHead(headMap);
        }
        LogDog.d("============== head start ================== ");
        for (String head : heads) {
            if (headMap.isEmpty()) {
                headMap.put(XHttpProtocol.XY_FIST_LINE, head);
                LogDog.d(head);
            } else {
                String[] kv = head.split(": ");
                if (kv.length == 2) {
                    headMap.put(kv[0], kv[1]);
                    LogDog.d(kv[0] + ":" + kv[1]);
                }
            }
        }
        LogDog.d("============== head end ================== ");
    }
}
