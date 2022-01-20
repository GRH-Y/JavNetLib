package com.currency.net.xhttp.utils;

import com.currency.net.base.RequestMode;
import com.currency.net.xhttp.entity.XRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * http 1.0/1.1
 */
public class XHttpProtocol {

    public static final String XY_HOST = "Host";
    public static final String XY_ACCEPT = "Accept";
    public static final String XY_COOKIE = "Cookie";
    public static final String XY_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String XY_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String XY_CONTENT_LENGTH = "Content-Length";
    public static final String XY_CACHE_CONTROL = "Cache-Control";
    public static final String XY_CONTENT_TYPE = "Content-Type";
    public static final String XY_CONNECTION = "Connection";
    public static final String XY_USER_AGENT = "User-Agent";
    public static final String XY_CONTENT_ENCODING = "Content-Encoding";
    public static final String XY_FIST_LINE = "Fist-Line";
    public static final String XY_LOCATION = "Location";
    public static final String XY_REFERER = "Referer";
    public static final String XY_TRANSFER_ENCODING = "Transfer-Encoding";


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
     * <p>
     * Location：这个头配合302状态吗，用于告诉客户端找谁
     * Server：服务器通过这个头，告诉浏览器服务器的类型
     * Content-Encoding：告诉浏览器，服务器的数据压缩格式
     * Content-Length：告诉浏览器，回送数据的长度
     * Content-Type：告诉浏览器，回送数据的类型
     * Last-Modified：告诉浏览器当前资源缓存时间
     * Refresh：告诉浏览器，隔多长时间刷新
     * Content-Disposition：告诉浏览器以下载的方式打开数据。例如： context.Response.AddHeader("Content-Disposition","attachment:filename=aa.jpg");                                        context.Response.WriteFile("aa.jpg");
     * Transfer-Encoding：告诉浏览器，传送数据的编码格式
     * ETag：缓存相关的头（可以做到实时更新）
     * Expries：告诉浏览器回送的资源缓存多长时间。如果是-1或者0，表示不缓存
     * Cache-Control：控制浏览器不要缓存数据   no-cache
     * Pragma：控制浏览器不要缓存数据          no-cache
     * <p>
     * Connection：响应完成后，是否断开连接。  close/Keep-Alive
     * Date：告诉浏览器，服务器响应时间
     */
    public static final String CONTENT_TYPE_FROM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";
    //需要在表单中进行文件上传时，就需要使用该格式
    public static final String CONTENT_TYPE_DATA = "multipart/form-data";

    private Map<Object, String> mHeadParameterMap = new LinkedHashMap<>();

    private boolean mIsDisableSysProperty = false;

    public void initProtocol(XRequest request) {
        mHeadParameterMap.clear();
        mIsDisableSysProperty = request.isDisableSysProperty();
        if (!mIsDisableSysProperty) {
            RequestMode requestMode = request.getRequestMode();
            XUrlMedia httpUrlMedia = request.getUrl();
            byte[] data = request.getSendData();
            String path = httpUrlMedia.getPath();
            if (requestMode == RequestMode.GET) {
                if (data != null) {
                    path = httpUrlMedia.getPath() + new String(data);
                }
            }
            mHeadParameterMap.put(requestMode.getMode(), " " + path + " HTTP/1.1\r\n");
            mHeadParameterMap.put(XY_HOST, httpUrlMedia.getHost());
            mHeadParameterMap.put(XY_ACCEPT, " */*");
            mHeadParameterMap.put(XY_ACCEPT_LANGUAGE, "*/*");
            mHeadParameterMap.put(XY_ACCEPT_ENCODING, "gzip, deflate");
            mHeadParameterMap.put(XY_USER_AGENT, "XHttp_1.0");
            mHeadParameterMap.put(XY_CONNECTION, "keep-alive");
            mHeadParameterMap.put(XY_REFERER, httpUrlMedia.getReferer());
            if (data != null && requestMode != RequestMode.GET) {
                mHeadParameterMap.put(XY_CONTENT_LENGTH, String.valueOf(data.length));
            }
        }

        Map<Object, Object> userParameter = request.getRequestProperty();
        if (userParameter != null && !userParameter.isEmpty()) {
            Set<Map.Entry<Object, Object>> set = userParameter.entrySet();
            for (Map.Entry<Object, Object> entry : set) {
                mHeadParameterMap.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }


    public Map<Object, String> getHeadParameterMap() {
        return mHeadParameterMap;
    }

    public void setHeadParameter(String key, String value) {
        mHeadParameterMap.put(key, value);
    }

    public void updateHeadParameter(String key, String value) {
        mHeadParameterMap.replace(key, value);
    }

    public void updatePath(String requestMode, String path) {
        mHeadParameterMap.replace(requestMode, " " + path + " HTTP/1.1\r\n");
    }

    public byte[] toByte() {
        if (mHeadParameterMap.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<Object, String> entry : mHeadParameterMap.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof RepeatableKey) {
                RepeatableKey repeatableKey = (RepeatableKey) key;
                key = repeatableKey.getKey();
            }
            if (isFirst && !mIsDisableSysProperty) {
                builder.append(key).append(entry.getValue());
                isFirst = false;
            } else {
                builder.append(key).append(": ").append(entry.getValue());
                if (!entry.getValue().endsWith("\r\n")) {
                    builder.append("\r\n");
                }
            }
        }
        builder.append("\r\n");
        return builder.toString().getBytes();
    }
}
