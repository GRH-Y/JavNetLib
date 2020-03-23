package connect.network.xhttp.entity;

import connect.network.xhttp.XHttpProtocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class XResponse {

    private byte[] httpData = null;
    private Map<String, String> httpHead = null;
    private Object result = null;

    public void setHttpData(byte[] httpData) {
        this.httpData = httpData;
    }

    public void setHttpHead(Map<String, String> headMap) {
        this.httpHead = headMap;
    }

    public byte[] getHttpData() {
        return httpData;
    }

    public <T> void setResult(T result) {
        this.result = result;
    }

    public byte[] getRawData() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (httpHead != null && !httpHead.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            boolean isFirst = true;
            for (Map.Entry<String, String> entry : httpHead.entrySet()) {
                if (isFirst) {
                    if (!XHttpProtocol.XY_FIST_LINE.equals(entry.getKey())) {
                        builder.append(entry.getKey());
                    }
                    builder.append(entry.getValue());
                    if (!entry.getValue().endsWith("\r\n")) {
                        builder.append("\r\n");
                    }
                    isFirst = false;
                } else {
                    builder.append(entry.getKey()).append(": ").append(entry.getValue());
                    if (!entry.getValue().endsWith("\r\n")) {
                        builder.append("\r\n");
                    }
                }
            }
            builder.append("\r\n");
            try {
                stream.write(builder.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] data = null;
        try {
            if (httpData != null) {
                stream.write(httpData);
            }
            data = stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public <T> T getResult() {
        return (T) result;
    }

    public Map<String, String> getHttpHead() {
        return httpHead;
    }

    public String getHeadForKey(String key) {
        String value = null;
        if (httpHead != null) {
            value = httpHead.get(key);
        }
        return value;
    }

    public void reset() {
        if (httpHead != null) {
            httpHead.clear();
        }
        httpData = null;
    }
}