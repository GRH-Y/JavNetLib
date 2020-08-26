package connect.network.xhttp.entity;

import connect.network.xhttp.utils.ByteCacheStream;

import java.util.LinkedHashMap;
import java.util.Map;

public class XResponse {

    private byte[] httpData = null;
    private ByteCacheStream raw;

    private Map<String, String> httpHead;
    private Object result = null;


    public XResponse() {
        raw = new ByteCacheStream();
        httpHead = new LinkedHashMap<>();
    }

    public void setHttpData(byte[] httpData) {
        this.httpData = httpData;
    }

    public byte[] getHttpData() {
        return httpData;
    }

    public <T> void setResult(T result) {
        this.result = result;
    }

    public void appendRawData(byte[] data) {
        if (data != null) {
            appendRawData(data, 0, data.length);
        }
    }

    public void appendRawData(byte[] data, int off, int len) {
        if (data != null) {
            raw.write(data, off, len);
        }
    }

    public ByteCacheStream getRawData() {
        return raw;
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
        raw.reset();
        result = null;
        httpData = null;
    }
}