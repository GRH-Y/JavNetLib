package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 请求创建链接
 *
 * @author yyz
 */
public class RequestProtocol extends ProxyProtocol {

    /**
     * 不包含length的4个字节的长度
     */
    private static final int HEAD_LENGTH = 75;

    public RequestProtocol(String channelId) {
        super(channelId);
    }

    @Override
    byte cmdType() {
        return CmdType.REQUEST.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        if (sendData() == null) {
            return null;
        }
        byte[] requestAdrByte = requestAdr();
        if (requestAdrByte == null) {
            return null;
        }
        int length = sendData().length + HEAD_LENGTH + requestAdrByte.length;
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(cmdType());
        srcData.put(channelId());
        srcData.put(requestId());
        srcData.put(requestAdrByte);
        return onEncrypt(encryptComponent, srcData);
    }
}
