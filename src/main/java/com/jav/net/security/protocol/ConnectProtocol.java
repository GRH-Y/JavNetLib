package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 请求创建链接
 *
 * @author yyz
 */
public class ConnectProtocol extends AbsProxyProtocol {

    /**
     * 成功状态码
     */
    public static final byte REP_SUCCESS_CODE = 0 ;
    /**
     * 异常状态码
     */
    public static final byte REP_EXCEPTION_CODE = 1 ;

    /**
     * 不包含length的4个字节的长度
     */
    private static final int HEAD_LENGTH = 73;

    public ConnectProtocol(String channelId) {
        super(channelId);
    }

    @Override
    byte cmdType() {
        return CmdType.REQUEST.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        byte[] requestAdrByte = requestAdr();
        byte[] sendData = sendData();
        if (requestAdrByte == null && sendData == null) {
            return null;
        }
        int length = HEAD_LENGTH;
        if (requestAdrByte != null) {
            length = length + requestAdrByte.length;
        } else if (sendData != null) {
            length = length + sendData.length;
        }
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(cmdType());
        srcData.put(channelId());
        srcData.put(requestId());
        if (requestAdrByte != null) {
            srcData.put(requestAdrByte);
        } else {
            srcData.put(sendData);
        }
        return onEncrypt(encryptComponent, srcData);
    }
}
