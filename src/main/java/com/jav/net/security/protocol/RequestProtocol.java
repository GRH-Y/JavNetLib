package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 请求创建链接
 *
 * @author yyz
 */
public class RequestProtocol extends AbsProxyProtocol {

    /**
     * 不包含length的4个字节的长度
     */
    private static final int HEAD_LENGTH = 73;

    /**
     * 真实的目标地址
     */
    private byte[] mRequestAdr;

    public RequestProtocol(String channelId) {
        super(channelId);
    }

    public void setRequestAdr(byte[] requestAdr) {
        this.mRequestAdr = requestAdr;
    }

    @Override
    byte cmdType() {
        return CmdType.REQUEST.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        byte[] sendData = sendData();
        if (mRequestAdr == null && sendData == null) {
            return null;
        }
        int length = HEAD_LENGTH;
        if (mRequestAdr != null) {
            length = length + mRequestAdr.length;
        } else if (sendData != null) {
            length = length + sendData.length;
        }
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(cmdType());
        srcData.put(channelId());
        srcData.put(requestId());
        if (mRequestAdr != null) {
            srcData.put(mRequestAdr);
        } else {
            srcData.put(sendData);
        }
        return onEncrypt(encryptComponent, srcData);
    }
}
