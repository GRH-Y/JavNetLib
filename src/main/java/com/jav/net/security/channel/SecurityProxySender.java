package com.jav.net.security.channel;

import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.net.base.AbsNetSender;
import com.jav.net.base.MultiBuffer;
import com.jav.net.security.channel.base.ConstantCode;
import com.jav.net.security.channel.joggle.ChannelEncryption;
import com.jav.net.security.channel.joggle.IChangeEncryptCallBack;
import com.jav.net.security.channel.joggle.ISecurityProxySender;
import com.jav.net.security.guard.SecurityChannelTraffic;
import com.jav.net.security.guard.SecurityMachineIdMonitor;
import com.jav.net.security.protocol.InitProtocol;
import com.jav.net.security.protocol.KeepAliveProtocol;
import com.jav.net.security.protocol.TransProtocol;
import com.jav.net.security.protocol.base.EncodeCode;
import com.jav.net.security.protocol.base.TransOperateCode;

import java.nio.ByteBuffer;

/**
 * 安全协议数据发送者
 *
 * @author yyz
 */
public class SecurityProxySender extends SecuritySender implements ISecurityProxySender {

    /**
     * machine id
     */
    private String mMachineId;

    public SecurityProxySender(AbsNetSender sender) {
        super(sender);
    }


    /**
     * 设置机器的id
     *
     * @param machineId 机器id
     */
    protected void setMachineId(String machineId) {
        mMachineId = machineId;
    }

    /**
     * 客户端向服务端发起init协议请求
     *
     * @param initData              如果是des加密方式即是传密码,不是为null
     * @param changeEncryption      需要切换的加密方式
     * @param changeEncryptCallBack 切换加密方式回调
     */
    public void requestInitData(byte[] initData, ChannelEncryption changeEncryption,
                                IChangeEncryptCallBack changeEncryptCallBack) {
        // 发送init协议数据
        InitProtocol initProtocol = new InitProtocol(mMachineId);
        initProtocol.setSendData(initData);
        ChannelEncryption.TransmitEncryption encryption =changeEncryption.getTransmitEncryption();
        EncryptionType encryptionType = encryption.getEncryptionType();
        initProtocol.setOperateCode(encryptionType.getCode());
        ByteBuffer encodeData = initProtocol.toData(mEncryptComponent);
        changeEncryptCallBack.onChange(changeEncryption);
        mCoreSender.sendData(new MultiBuffer(encodeData));
        SecurityChannelTraffic.getInstance().monitorTraffic(mMachineId, 0, encodeData.limit());
    }

    /**
     * 服务端向客户端响应init协议请求
     *
     * @param machineId 机器id
     * @param repCode   响应码
     * @param initData  数据
     */
    @Override
    public void respondToInitRequest(String machineId, byte repCode, byte[] initData) {
        InitProtocol initProtocol = new InitProtocol(machineId);
        initProtocol.setOperateCode(repCode);
        initProtocol.setSendData(initData);
        ByteBuffer encodeData = initProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
        SecurityChannelTraffic.getInstance().monitorTraffic(mMachineId, 0, encodeData.limit());
    }

    /**
     * 请求链接真实目标
     *
     * @param requestId 请求id
     * @param address   真实目标地址
     */
    @Override
    public void sendRequestData(String requestId, byte[] address) {
        if (requestId == null || address == null) {
            return;
        }
        TransProtocol connectProtocol = new TransProtocol(mMachineId, requestId);
        connectProtocol.setOperateCode(TransOperateCode.ADDRESS.getCode());
        connectProtocol.setSendData(address);
        ByteBuffer encodeData = connectProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
        SecurityChannelTraffic.getInstance().monitorTraffic(mMachineId, 0, encodeData.limit());
    }

    /**
     * 响应 connect 请求
     *
     * @param requestId
     * @param status
     */
    @Override
    public void respondToRequest(String requestId, byte status) {
        if (requestId == null) {
            return;
        }
        TransProtocol connectProtocol = new TransProtocol(mMachineId, requestId);
        byte operateCode = (byte) (status | TransOperateCode.ADDRESS.getCode());
        connectProtocol.setOperateCode(operateCode);
        ByteBuffer encodeData = connectProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
        SecurityChannelTraffic.getInstance().monitorTraffic(mMachineId, 0, encodeData.limit());
    }

    /**
     * 中转数据
     *
     * @param requestId 请求id
     * @param data      转发的数据
     */
    @Override
    public void sendTransData(String requestId, byte[] data) {
        if (requestId == null || data == null) {
            return;
        }
        TransProtocol transProtocol = new TransProtocol(mMachineId, requestId);
        transProtocol.setOperateCode(TransOperateCode.DATA.getCode());
        transProtocol.setSendData(data);
        ByteBuffer encodeData = transProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
        SecurityChannelTraffic.getInstance().monitorTraffic(mMachineId, 0, encodeData.limit());
    }

    /**
     * 相应 trans 请求
     *
     * @param requestId 异常的情况可以返回任意32 bit长度的内容
     * @param status    异常返回64，正常返回0
     * @param data      异常情况可以返回null
     * @see ConstantCode status
     */
    @Override
    public void respondToTrans(String requestId, byte status, byte[] data) {
        TransProtocol transProtocol = new TransProtocol(mMachineId, requestId);
        byte operateCode = (byte) (status | TransOperateCode.DATA.getCode());
        transProtocol.setOperateCode(operateCode);
        transProtocol.setSendData(data);
        ByteBuffer encodeData = transProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
        SecurityChannelTraffic.getInstance().monitorTraffic(mMachineId, 0, encodeData.limit());
    }

    @Override
    public void sendKeepAlive(String machineId) {
        KeepAliveProtocol keepAliveProtocol = new KeepAliveProtocol(machineId);
        ByteBuffer encodeData = keepAliveProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
        SecurityChannelTraffic.getInstance().monitorTraffic(mMachineId, 0, encodeData.limit());
    }

}
