package com.jav.net.security.protocol;

import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.common.util.StringEnvoy;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 代理协议
 *
 * @author yyz
 */
public abstract class AbsProxyProtocol {

    public static final int MACHINE_LENGTH = 32;

    public static final int CHANNEL_LENGTH = 32;

    public static final int REQUEST_LENGTH = 32;


    /**
     * 成功状态码
     */
    public static final byte REP_SUCCESS_CODE = 0 ;
    /**
     * 异常状态码
     */
    public static final byte REP_EXCEPTION_CODE = 1 ;


    /**
     * 编码类型
     */
    protected enum EnType {
        /**
         * 不编码
         */
        NO_ENCODE((byte) 0),
        /**
         * Base64编码
         */
        BASE64((byte) 1),
        /**
         * AES编码
         */
        AES((byte) 2),
        /**
         * 其它
         */
        OTHER((byte) 3);

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


    public AbsProxyProtocol(String channelId) {
        if (StringEnvoy.isEmpty(channelId)) {
            throw new IllegalArgumentException("channel id can not be null !!!");
        }
        setChannelId(channelId.getBytes());
    }

    public AbsProxyProtocol(String machineId, byte[] data) {
        this.mMachineId = machineId.getBytes();
        updateSendData(data);
    }

    protected long time() {
        return System.currentTimeMillis();
    }

    /**
     * 当前协议对应的命令类型
     *
     * @return
     * @see CmdType
     */
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
