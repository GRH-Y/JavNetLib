package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 初始化接口，服务端校验客户端，客户端获得随机数
 *
 * @author yyz
 */
public class InitProtocol extends AbsProxyProtocol {

    /**
     * 不包含length的4个字节的长度
     */
    private static final int HEAD_LENGTH = 42;

    /**
     * 响应code
     */
    private Byte mRepCode;

    public InitProtocol(String machineId, byte repCode, byte[] initData) {
        super(machineId, initData);
        setEnType(EnType.BASE64.getType());
        this.mRepCode = repCode;
    }

    public InitProtocol(String machineId, byte[] initData) {
        super(machineId, initData);
        setEnType(EnType.BASE64.getType());
    }

    @Override
    byte cmdType() {
        return CmdType.INIT.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        int length = HEAD_LENGTH;
        if (sendData() != null) {
            length += sendData().length;
        }
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(cmdType());
        srcData.put(machineId());
        if (mRepCode != null) {
            srcData.put(mRepCode);
        } else {
            srcData.put(encryptionType());
        }
        if (sendData() != null) {
            srcData.put(sendData());
        }
        return onEncrypt(encryptComponent, srcData);
    }

}
