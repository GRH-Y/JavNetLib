package com.jav.net.security.protocol;

import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.common.util.StringEnvoy;

import java.nio.ByteBuffer;

public abstract class ProxyProtocol {

    public static final int MACHINE_LENGTH = 32;

    public static final int CHANNEL_LENGTH = 32;

    public static final int REQUEST_LENGTH = 32;


    /**
     * 编码类型
     */
    protected enum EnType {

        NO_ENCODE((byte) 0), BASE64((byte) 1), DES((byte) 2), OTHER((byte) 3);

        private final byte mType;

        EnType(byte type) {
            mType = type;
        }

        public byte getType() {
            return mType;
        }
    }


    /**
     * 机器码（32Byte）
     */
    private byte[] mMachineId;

    /**
     * 发送的数据
     */
    private byte[] mSendData;

//    /**
//     * 配置数据用途的类型（1Byte）
//     */
//    private byte mCmdType;

    /**
     * 数据编码类型，配置数据加密类型（1Byte）
     */
    private byte mEncryptionType;

    /**
     * 通道id，区分不同的客户端，由服务端init数据生成返回
     */
    private byte[] mChannelId;

    /**
     * 请求id，区分http请求的链路
     */
    private byte[] mRequestId;

    /**
     * 用于udp记录包的顺序,最大255个包，超出服务器会重置
     */
    private byte mPacketOrder = 0;

    /**
     * 真实的目标地址
     */
    private byte[] mRequestAdr;


    public ProxyProtocol(String channelId) {
        if (StringEnvoy.isEmpty(channelId)) {
            throw new IllegalArgumentException("channel id can not be null !!!");
        }
        setChannelId(channelId.getBytes());
    }

    public ProxyProtocol(String machineId, byte[] data) {
        this.mMachineId = machineId.getBytes();
        updateSendData(data);
    }

    protected long time() {
        return System.currentTimeMillis();
    }

    abstract byte cmdType();

    protected byte encryptionType() {
        return mEncryptionType;
    }

    protected byte[] machineId() {
        return mMachineId;
    }

    protected byte[] requestId() {
        return mRequestId;
    }

    protected byte[] channelId() {
        return mChannelId;
    }

    protected byte packetOrder() {
        return mPacketOrder;
    }

    protected byte[] sendData() {
        return mSendData;
    }

    protected byte[] requestAdr() {
        return mRequestAdr;
    }

    public void setEnType(byte mEnType) {
        this.mEncryptionType = mEnType;
    }

    public void setChannelId(byte[] mChannelId) {
        this.mChannelId = mChannelId;
    }

    public void setRequestId(byte[] mRequestId) {
        this.mRequestId = mRequestId;
    }

    public void setPacketOrder(byte mPacketOrder) {
        this.mPacketOrder = mPacketOrder;
    }

    public void setRequestAdr(byte[] requestAdr) {
        this.mRequestAdr = requestAdr;
    }

    public void updateSendData(byte[] sendData) {
        this.mSendData = sendData;
    }

    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        return null;
    }


    protected ByteBuffer onEncrypt(IEncryptComponent component, ByteBuffer srcData) {
        byte[] data = srcData.array();
        byte[] encodeData = data;
        if (component != null) {
            encodeData = component.onEncrypt(data);
        }
        ByteBuffer finalData = ByteBuffer.allocate(encodeData.length + 4);
        finalData.putInt(encodeData.length);
        finalData.put(encodeData);
        return finalData;
    }
}
