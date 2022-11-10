package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 传输数据
 *
 * @author yyz
 */
public class TransProtocol extends ProxyProtocol {

    /**
     * 不包含length的4个字节的长度
     */
    private static final int HEAD_LENGTH = 74;

    public TransProtocol(String channelId) {
        super(channelId);
    }

    @Override
    byte cmdType() {
        return CmdType.TRANS.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        if (sendData() == null) {
            return null;
        }

        int length = sendData().length + HEAD_LENGTH;
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(cmdType());
        srcData.put(channelId());
        srcData.put(requestId());
        srcData.put(packetOrder());
        srcData.put(sendData());

        return onEncrypt(encryptComponent, srcData);
    }
}
