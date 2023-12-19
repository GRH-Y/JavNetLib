package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.common.util.StringEnvoy;
import com.jav.net.security.protocol.base.ActivityCode;

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

    /**
     * 通道id，区分不同的客户端，由服务端init数据生成返回
     */
    private final byte[] mMachineId;


    public KeepAliveProtocol(String machineId) {
        if (StringEnvoy.isEmpty(machineId)) {
            throw new IllegalArgumentException("machineId can not be null !!!");
        }
        this.mMachineId = machineId.getBytes();
    }


    @Override
    public byte activityCode() {
        return ActivityCode.KEEP.getCode();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        int length = HEAD_LENGTH;
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(activityCode());
        srcData.put(mMachineId);
        return onEncrypt(encryptComponent, srcData);
    }
}
