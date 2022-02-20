package com.currency.net.xhttp.entity;

import com.currency.net.xhttp.utils.ByteCacheStream;

import java.util.LinkedHashMap;
import java.util.Map;

public class XResponse {

    /**
     * 完整的http数据
     */
    private final ByteCacheStream mRaw;
    /**
     * http head
     */
    private final Map<String, String> mHttpHead;
    /**
     * http body
     */
    private byte[] mHttpData = null;

    /**
     * body转换成实体结果
     */
    private Object mResult = null;


    public XResponse() {
        mRaw = new ByteCacheStream();
        mHttpHead = new LinkedHashMap<>();
    }

    public void setHttpData(byte[] httpData) {
        this.mHttpData = httpData;
    }

    public byte[] getHttpData() {
        return mHttpData;
    }

    public <T> void setResult(T result) {
        this.mResult = result;
    }

    public void appendRawData(byte[] data) {
        if (data != null) {
            appendRawData(data, 0, data.length);
        }
    }

    public void appendRawData(byte[] data, int off, int len) {
        if (data != null) {
            mRaw.write(data, off, len);
        }
    }

    public ByteCacheStream getRawData() {
        return mRaw;
    }

    public <T> T getResult() {
        return (T) mResult;
    }

    public Map<String, String> getHttpHead() {
        return mHttpHead;
    }

    public String getHeadForKey(String key) {
        return mHttpHead.get(key);
    }

    public void reset() {
        mHttpHead.clear();
        mRaw.reset();
    }
}