package com.jav.net.security.channel.base;

/**
 * 常量码
 *
 * @author yyz
 */
public class ConstantCode {

    public final static int NORMAL_ADDRESS_LENGTH = 2;

    public static final int MACHINE_LENGTH = 32;

    public static final int CHANNEL_LENGTH = 32;

    public static final int REQUEST_LENGTH = 32;


    /**
     * 成功状态码
     */
    public static final byte REP_SUCCESS_CODE = 0;

    /**
     * 异常状态码，0100 0000b，最高位是符号位，所以用第二位作为响应码状态位
     */
    public static final byte REP_EXCEPTION_CODE = 64;


}
