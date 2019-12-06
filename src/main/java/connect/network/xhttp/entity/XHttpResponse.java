package connect.network.xhttp.entity;

import connect.network.xhttp.HttpProtocol;
import storage.GZipUtils;
import util.StringEnvoy;

import java.io.ByteArrayOutputStream;
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
        if (data != null && data.length > 0) {
            response.setRaw(data);
            Map<String, String> head = new LinkedHashMap<>();
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
            response.setHead(head);
            String length = head.get(HttpProtocol.XY_CONTENT_LENGTH);
            String encode = head.get(HttpProtocol.XY_CONTENT_ENCOD);

            if (StringEnvoy.isEmpty(length)) {
                int dataStartIndex = content[0].length() + 4;
                String[] responseData = content[1].split("\r\n");
                try (ByteArrayOutputStream responseDataStream = new ByteArrayOutputStream()) {
                    for (int index = 0; index < responseData.length; index += 2) {
                        if (responseData[index].length() < 8) {
                            dataStartIndex += responseData[index].length() + 2;
                            int dataLength = Integer.parseInt(responseData[index], 16);
                            if (dataLength > 0) {
                                responseDataStream.write(data, dataStartIndex, dataLength);
                                dataStartIndex += dataLength + 2;
                            }
                        }
                    }
                    data = responseDataStream.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if ("gzip".equals(encode)) {
                byte[] unZip = GZipUtils.unCompress(data);
                if (unZip != null) {
                    response.setData(new String(unZip));
                }
            } else {
                response.setData(content[1]);
            }

        }
        return response;
    }

}