package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.common.util.StringEnvoy;
import com.jav.net.security.protocol.base.ActivityCode;

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
     * 机器id，区分不同的客户
     */
    private final byte[] mMachineId;


    /**
     * 请求id，区分http请求的链路
     */
    private final byte[] mRequestId;


    public TransProtocol(String channelId, String requestId) {
        if (StringEnvoy.isEmpty(channelId) || StringEnvoy.isEmpty(requestId)) {
            throw new IllegalArgumentException("channelId or requestId can not be null !!!");
        }
        this.mMachineId = channelId.getBytes();
        this.mRequestId = requestId.getBytes();
    }


    @Override
    public byte activityCode() {
        return ActivityCode.TRANS.getCode();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        int length = HEAD_LENGTH;
        byte[] sendData = sendData();
        if (sendData != null) {
            length += sendData.length;
        }

        ByteBuffer srcData = ByteBuffer.allocate(length);

        srcData.putLong(time());
        srcData.put(activityCode());
        srcData.put(mMachineId);
        srcData.put(operateCode());
        srcData.put(mRequestId);
        if (sendData != null) {
            srcData.put(sendData);
        }

        return onEncrypt(encryptComponent, srcData);
    }
}
