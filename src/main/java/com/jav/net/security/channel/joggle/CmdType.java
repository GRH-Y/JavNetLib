package com.jav.net.security.channel.joggle;

/**
 * 命令类型，用于区分当前数据载体的类型
 *
 * @author yyz
 */
public enum CmdType {

    //初始化
    INIT((byte) 1),
    /**
     * 创建链接
     */
    REQUEST((byte) 2),
    /**
     * 传输数据
     */
    TRANS((byte) 4),
    /**
     * 同步服务
     */
    SYNC((byte) 8);

    private byte mCmd;

    CmdType(byte cmd) {
        mCmd = cmd;
    }

    public byte getCmd() {
        return mCmd;
    }
}