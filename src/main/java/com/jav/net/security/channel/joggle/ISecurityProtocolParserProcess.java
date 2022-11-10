package com.jav.net.security.channel.joggle;

import java.nio.ByteBuffer;

/**
 * 安全协议解析监听器
 *
 * @author yyz
 */
public interface ISecurityProtocolParserProcess {

    /**
     * 解密数据
     *
     * @param encodeData
     * @return
     */
    ByteBuffer decodeData(ByteBuffer encodeData);

    /**
     * 校验时间,48小时时间差都有效
     *
     * @param time
     * @return 返回true为校验通过
     */
    boolean onCheckTime(long time);

    /**
     * 校验机器id
     *
     * @param mid
     * @return 返回true为校验通过
     */
    boolean onCheckMachineId(String mid);


    /**
     * 校验channel id
     *
     * @param channelId
     * @return
     */
    boolean onCheckChannelId(String channelId);


    /**
     * 根据cmd处理不同的数据包
     *
     * @param cmd
     * @param data
     */
    void onExecCmd(byte cmd, ByteBuffer data);

    /**
     * 校验不通过回调
     *
     * @param msg
     */
    void onDeny(String msg);

    /**
     * 出现异常错误回调
     */
    void onError();
}
