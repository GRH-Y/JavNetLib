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
    private byte[] mChannelId;


    public KeepAliveProtocol(String channelId) {
        if (StringEnvoy.isEmpty(channelId)) {
            throw new IllegalArgumentException("channelId can not be null !!!");
        }
        this.mChannelId = channelId.getBytes();
    }


    @Override
    byte activityCode() {
        return ActivityCode.KEEP.getCode();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        int length = HEAD_LENGTH;
        ByteBuffer srcData = ByteBuffer.allocate(length);
        srcData.putLong(time());
        srcData.put(activityCode());
        srcData.put(mChannelId);
        return onEncrypt(encryptComponent, srcData);
    }
}
