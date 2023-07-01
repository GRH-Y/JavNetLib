package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 传输数据
 *
 * @author yyz
 */
public class TransProtocol extends AbsProxyProtocol {

    /**
     * 不包含length的4个字节的长度
     */
    private static final int HEAD_LENGTH = 74;

    /**
     * 响应码,只用于服务端响应客户端,0 正常,1 异常
     */
    private Byte repCode;

    public TransProtocol(String channelId) {
        super(channelId);
    }


    public void setRepCode(byte repCode) {
        this.repCode = repCode;
    }

    @Override
    byte cmdType() {
        return CmdType.TRANS.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        int length = HEAD_LENGTH;
        if (sendData() != null) {
            length += sendData().length;
        }
        if (repCode != null) {
            length++;
        }
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(cmdType());
        srcData.put(channelId());
        srcData.put(requestId());
        srcData.put(packetOrder());
        if (repCode != null) {
            srcData.put(repCode);
        }
        if (sendData() != null) {
            srcData.put(sendData());
        }
        return onEncrypt(encryptComponent, srcData);
    }
}
