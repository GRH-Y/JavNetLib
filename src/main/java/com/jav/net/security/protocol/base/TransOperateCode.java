package com.jav.net.security.protocol.base;

/**
 * trans operate code定义
 */
public enum TransOperateCode {

    /**
     * 地址
     */
    ADDRESS((byte) 0),

    /**
     * 中转数据
     */
    DATA((byte) 1);


    private final byte mCode;

    TransOperateCode(byte code) {
        mCode = code;
    }

    public byte getCode() {
        return mCode;
    }

    public static TransOperateCode getInstance(byte code) {
        if (code == ADDRESS.mCode) {
            return ADDRESS;
        } else if (code == DATA.mCode) {
            return DATA;
        }
        return null;
    }
}