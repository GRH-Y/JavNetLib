package com.jav.net.security.protocol.base;

/**
 * 编码格式
 */
public enum EncodeCode {
    /**
     * 不编码
     */
    NO_ENCODE((byte) 0),
    /**
     * Base64编码
     */
    BASE64((byte) 1),
    /**
     * AES编码
     */
    AES((byte) 2),
    /**
     * 其它
     */
    OTHER((byte) 3);

    private final byte mType;

    EncodeCode(byte type) {
        mType = type;
    }

    public byte getType() {
        return mType;
    }

    public static EncodeCode getInstance(byte type) {
        if (type == NO_ENCODE.mType) {
            return NO_ENCODE;
        } else if (type == BASE64.mType) {
            return BASE64;
        } else if (type == AES.mType) {
            return AES;
        } else if (type == OTHER.mType) {
            return OTHER;
        }
        return null;
    }
}