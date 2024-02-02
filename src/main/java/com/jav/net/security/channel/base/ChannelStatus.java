package com.jav.net.security.channel.base;

/**
 * 通道状态
 *
 * @author yyz
 */
public enum ChannelStatus {
    //空闲状态
    NONE(0),
    //初始化（跟服务端协商交换密钥）
    INIT(1),
    //通道就绪
    READY(2),
    //通道销毁
    INVALID(4);

    private int mCode;

    ChannelStatus(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}