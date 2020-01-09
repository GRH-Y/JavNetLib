package connect.network.xhttp.entity;

import connect.network.xhttp.HttpProtocol;

import java.util.Map;

public class XHttpResponse {

    private byte[] raw = null;
    private String data = null;
    private Exception exception = null;
    private Map<String, String> headMap = null;

    public void setRaw(byte[] raw) {
        this.raw = raw;
    }

    public int getCode() {
        int code = 0;
        if (headMap != null) {
            String content = headMap.get(HttpProtocol.XY_RESPONSE_CODE);
            if (content.startsWith("HTTP")) {
                String[] arrays = content.split(" ");
                if (arrays.length > 1) {
                    try {
                        code = Integer.parseInt(arrays[1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return code;
    }

    public String getVersion() {
        String version = null;
        if (headMap != null) {
            String content = headMap.get(HttpProtocol.XY_RESPONSE_CODE);
            String[] arrays = content.split(" ");
            if (arrays.length > 1) {
                version = arrays[0];
            }
        }
        return version;
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

    public void reset() {
        if (headMap != null) {
            headMap.clear();
        }
        exception = null;
        data = null;
        raw = null;
    }
}