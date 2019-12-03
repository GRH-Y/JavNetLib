package connect.network.xhttp;

import connect.network.http.RequestMode;
import connect.network.xhttp.entity.XHttpRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * http 1.0/1.1
 */
public class HttpProtocol {

    public static final String XY_HOST = "Host";
    public static final String XY_ACCEPT = "Accept";
    public static final String XY_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String XY_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String XY_CONTENT_LENGTH = "Content-Length";
    public static final String XY_CACHE_CONTROL = "Cache-Control";
    public static final String XY_CONNECTION = "Connection";
    public static final String XY_USER_AGENT = "User-Agent";
    public static final String XY_CONTENT_ENCOD = "Content-Encoding";


    /**
     * 常见的媒体格式类型如下：
     * <p>
     * text/html ： HTML格式
     * text/plain ：纯文本格式
     * text/xml ： XML格式
     * image/gif ：gif图片格式
     * image/jpeg ：jpg图片格式
     * image/png：png图片格式
     * <p>
     * 以application开头的媒体格式类型：
     * application/xhtml+xml ：XHTML格式
     * application/xml： XML数据格式
     * application/atom+xml ：Atom XML聚合格式
     * application/json： JSON数据格式
     * application/pdf：pdf格式
     * application/msword ： Word文档格式
     * application/octet-stream ： 二进制流数据（如常见的文件下载）
     * application/x-www-form-urlencoded ： <form encType=””>中默认的encType，form表单数据被编码为key/value格式发送到服务器（表单默认的提交数据的格式）
     * <p>
     * 另外一种常见的媒体格式是上传文件之时使用的：
     * multipart/form-data ： 需要在表单中进行文件上传时，就需要使用该格式
     */
    public static final String CONTENT_TYPE_FROM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";

    private Map<String, String> headParameterMap;


    public HttpProtocol(XHttpRequest requestEntity) {
        headParameterMap = new LinkedHashMap<>();
        RequestMode requestMode = requestEntity.getRequestMode();
        headParameterMap.put(requestMode.getMode(), " / HTTP/1.1\r\n");
        headParameterMap.put(XY_HOST, requestEntity.getAddress());
        headParameterMap.put(XY_ACCEPT, "*/*");
        headParameterMap.put(XY_ACCEPT_LANGUAGE, "*/*");
        headParameterMap.put(XY_ACCEPT_ENCODING, "gzip, deflate");
        headParameterMap.put(XY_USER_AGENT, "XHttp_1.0");
        headParameterMap.put(XY_CONNECTION, "keep-alive");
        byte[] data = requestEntity.getSendData();
        headParameterMap.put(XY_CONTENT_LENGTH, data == null ? "0" : String.valueOf(data.length));
    }

    public Map<String, String> getHeadParameterMap() {
        return headParameterMap;
    }

    public void setHeadParameter(String key, String value) {
        headParameterMap.put(key, value);
    }

    public void setUserParameter(Map<String, Object> userParameter) {
        if (userParameter != null && !userParameter.isEmpty()) {
            Set<Map.Entry<String, Object>> set = userParameter.entrySet();
            for (Map.Entry<String, Object> entry : set) {
                headParameterMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }

    public byte[] toByte() {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : headParameterMap.entrySet()) {
            if (isFirst) {
                builder.append(entry.getKey()).append(entry.getValue());
                isFirst = false;
            } else {
                builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
        }
        builder.append("\r\n\r\n");
        return builder.toString().getBytes();
    }
}
