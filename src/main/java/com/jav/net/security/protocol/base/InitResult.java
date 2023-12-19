package com.jav.net.security.protocol.base;

/**
 * init协议结果类型
 *
 * @author yyz
 */
public enum InitResult {

    /**
     * 出错
     */
    ERROR((byte) 0),
    /**
     * 返回channel id
     */
    OK((byte) 1),
    /**
     * 返回新的服务ip,通常是服务负载过高
     */
    SERVER_IP((byte) 2);


    private final byte mCode;

    InitResult(byte code) {
        mCode = code;
    }

    public byte getCode() {
        return mCode;
    }

    public static InitResult getInstance(byte code) {
        if (OK.getCode() == code) {
            return OK;
        } else if (SERVER_IP.getCode() == code) {
            return SERVER_IP;
        }
        return null;
    }
}