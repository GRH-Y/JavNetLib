package connect.network.xhttp.entity;

import java.util.Map;

public class XHttpResponse {

    private byte[] raw = null;
    private String data = null;
    private Exception exception = null;
    private Map<String, String> headMap = null;

    public void setRaw(byte[] raw) {
        this.raw = raw;
    }

    public void setHeadMap(Map<String, String> headMap) {
        this.headMap = headMap;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte[] getRaw() {
        return raw;
    }

    public Exception getException() {
        return exception;
    }

    public Map<String, String> getHeadMap() {
        return headMap;
    }

    public String getData() {
        return data;
    }

    public String getHeadForKey(String key) {
        String value = null;
        if (headMap != null) {
            value = headMap.get(key);
        }
        return value;
    }
}