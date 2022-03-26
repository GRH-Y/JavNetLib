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
     * http body 在整个数据的起始地址
     */
    private int mDataIndex;

    /**
     * http body 数据的长度
     */
    private int mDataLength;

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

    public void setHttpDataInfo(int dataIndex, int dataLength) {
        this.mDataIndex = dataIndex;
        this.mDataLength = dataLength;
    }

    public byte[] getHttpData() {
        if (mHttpData != null) {
            return mHttpData;
        }
        if (mDataLength > 0) {
            byte[] data = mRaw.getBuf();
            mHttpData = new byte[mDataLength];
            System.arraycopy(data, mDataIndex, mHttpData, 0, mHttpData.length);
        }
        return mHttpData;
    }

    public <T> void setResult(T result) {
        this.mResult = result;
    }

    public void appendRawData(byte[] data) {
        appendRawData(data, 0, data.length);
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