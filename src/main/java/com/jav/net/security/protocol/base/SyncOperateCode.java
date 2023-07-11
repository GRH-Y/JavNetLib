package com.jav.net.security.protocol.base;

/**
 * sync operate code 定义
 */
public enum SyncOperateCode {

    /**
     * sync avg
     */
    SYNC_AVG((byte) 0),

    /**
     * sync mid
     */
    SYNC_MID((byte) 1);

    private final byte mCode;

    SyncOperateCode(byte type) {
        mCode = type;
    }

    public byte getCode() {
        return mCode;
    }

    public static SyncOperateCode getInstance(byte code) {
        if (code == SYNC_AVG.mCode) {
            return SYNC_AVG;
        } else if (code == SYNC_MID.mCode) {
            return SYNC_MID;
        }
        return null;
    }
}