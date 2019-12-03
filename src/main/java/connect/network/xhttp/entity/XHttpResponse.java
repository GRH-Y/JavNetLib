package connect.network.xhttp.entity;

import connect.network.xhttp.HttpProtocol;
import storage.GZipUtils;
import util.StringEnvoy;

import java.util.LinkedHashMap;
import java.util.Map;

public class XHttpResponse {


    private byte[] raw = null;
    private String data = null;
    private Exception exception = null;
    private Map<String, String> head = null;

    public void setRaw(byte[] raw) {
        this.raw = raw;
    }

    public void setHead(Map<String, String> head) {
        this.head = head;
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

    public Map<String, String> getHead() {
        return head;
    }

    public String getData() {
        return data;
    }

    public static XHttpResponse parsing(byte[] data) {
        XHttpResponse response = new XHttpResponse();
        Map<String, String> head = null;
        if (data != null) {
            response.setRaw(data);
            head = new LinkedHashMap<>();
            String[] content = new String(data).split("\r\n\r\n");
            String[] headMap = content[0].split("\r\n");
            boolean isFirst = true;
            for (String str : headMap) {
                if (isFirst) {
                    isFirst = false;
                    head.put("response_code: ", str);
                } else {
                    String[] args = str.split(": ");
                    if (args.length == 2) {
                        head.put(args[0], args[1]);
                    }
                }
            }
            String length = head.get(HttpProtocol.XY_CONTENT_LENGTH);
            String encode = head.get(HttpProtocol.XY_CONTENT_ENCOD);
            int dataLength = 0;
            int dataStartIndex = content[0].length() + 4;
            if (StringEnvoy.isEmpty(length)) {
                //找不到数据的长度，则需要在数据区查找
                for (int dataIndex = dataStartIndex; dataIndex < data.length; dataIndex++) {
                    if (data[dataIndex] == 13) {
                        if (data[dataIndex + 1] == 10) {
                            dataLength = Integer.parseInt(new String(data, dataStartIndex, dataIndex - dataStartIndex), 16);
                            dataStartIndex = dataIndex + 2;
                            break;
                        }
                    }
                }
            } else {
                dataLength = Integer.parseInt(length);
            }
            if ("gzip".equals(encode)) {
                byte[] unZip = GZipUtils.unCompress(data, dataStartIndex, dataLength);
                if (unZip != null) {
                    response.setData(new String(unZip));
                }
            } else {
                response.setData(content[1]);
            }
        }
        response.setHead(head);
        return response;
    }

}