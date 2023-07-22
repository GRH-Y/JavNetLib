package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.common.util.StringEnvoy;
import com.jav.net.security.protocol.base.ActivityCode;

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
     * 机器码（32Byte）
     */
    private byte[] mMachineId;


    public InitProtocol(String machineId) {
        if (StringEnvoy.isEmpty(machineId)) {
            throw new IllegalArgumentException("machineId id can not be null !!!");
        }
        this.mMachineId = machineId.getBytes();
    }

    @Override
    public byte activityCode() {
        return ActivityCode.INIT.getCode();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        int length = HEAD_LENGTH;
        if (sendData() != null) {
            length += sendData().length;
        }
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(activityCode());
        srcData.put(mMachineId);
        srcData.put(operateCode());

        if (sendData() != null) {
            srcData.put(sendData());
        }
        return onEncrypt(encryptComponent, srcData);
    }

}
