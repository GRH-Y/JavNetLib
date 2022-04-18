package com.currency.net.base;

import com.currency.net.entity.MultiByteBuffer;

import java.nio.ByteBuffer;

/**
 * 发送数据包装类
 *
 * @author yyz
 */
public class SendPacket {
    private final Object mSendData;

    private SendPacket(Object data) {
        mSendData = data;
    }

    public <T> T getSendData() {
        return (T) mSendData;
    }

    public static SendPacket getInstance(byte[] data) {
        if (data == null) {
            return null;
        }
        return new SendPacket(data);
    }

    public static SendPacket getInstance(ByteBuffer data) {
        if (data == null) {
            return null;
        }
        return new SendPacket(data);
    }

    public static SendPacket getInstance(MultiByteBuffer data) {
        if (data == null) {
            return null;
        }
        return new SendPacket(data);
    }
}
