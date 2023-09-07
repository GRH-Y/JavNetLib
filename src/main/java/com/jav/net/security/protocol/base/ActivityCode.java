package com.jav.net.security.protocol.base;

/**
 * 命令类型，用于区分当前数据载体的类型
 *
 * @author yyz
 */
public enum ActivityCode {

    //初始化
    INIT((byte) 1),
    /**
     * 传输数据
     */
    TRANS((byte) 2),

    /**
     * KeepAlive
     */
    KEEP((byte) 8);

    private final byte mCode;

    ActivityCode(byte code) {
        mCode = code;
    }

    public byte getCode() {
        return mCode;
    }


    public static ActivityCode getInstance(byte code) {
        if (code == INIT.mCode) {
            return INIT;
        } else if (code == TRANS.mCode) {
            return TRANS;
        } else if (code == KEEP.mCode) {
            return KEEP;
        }
        return null;
    }
}