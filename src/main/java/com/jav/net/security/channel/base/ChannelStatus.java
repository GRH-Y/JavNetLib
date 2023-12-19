package com.jav.net.security.channel.base;

/**
 * 通道状态
 *
 * @author yyz
 */
public enum ChannelStatus {
    //空闲状态
    NONE(0),
    //通道就绪
    READY(1),
    //通道销毁
    INVALID(2);

    private int mCode;

    ChannelStatus(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}