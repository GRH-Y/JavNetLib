package com.jav.net.security.channel.base;

public enum UnusualBehaviorType {

    EXP_TIME(0x109, "Illegal time"),

    EXP_LENGTH(0x110, "Illegal length"),

    EXP_TRANS_ADDRESS(0x111, "illegal trans address"),

    EXP_TRANS_DATA(0x112, "illegal trans data"),

    EXP_ADDRESS_LENGTH(0x113, "Illegal address length"),

    EXP_REQUEST_ID(0x114, "illegal requestId"),

    EXP_OPERATE_CODE(0x115, "illegal operate code"),

    EXP_CHANNEL_ID(0x116, "illegal channelId"),

    EXP_MACHINE_ID(0x117, "illegal machineId"),

    EXP_ACTIVITY(0x118, "illegal activity"),

    EXP_INIT_DATA(0x119, "illegal init data");


    private final String mErrorMsg;
    private final int mErrorCode;

    UnusualBehaviorType(int errorCode, String errorMsg) {
        this.mErrorCode = errorCode;
        this.mErrorMsg = errorMsg;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorMsg() {
        return mErrorMsg;
    }
}
