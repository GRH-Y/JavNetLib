package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * KeepAlive 心跳
 *
 * @author yyz
 */
public class KeepAliveProtocol extends AbsProxyProtocol {

    /**
     * 不包含length的4个字节的长度
     */
    private static final int HEAD_LENGTH = 41;


    public KeepAliveProtocol(String channelId) {
        super(channelId);
    }


    @Override
    byte cmdType() {
        return CmdType.KEEP.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        int length = HEAD_LENGTH;
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(cmdType());
        srcData.put(channelId());
        return onEncrypt(encryptComponent, srcData);
    }
}
