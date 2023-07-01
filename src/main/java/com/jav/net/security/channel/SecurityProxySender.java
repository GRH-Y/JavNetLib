package com.jav.net.security.channel;

import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.net.entity.MultiByteBuffer;
import com.jav.net.security.channel.joggle.IChangeEncryptCallBack;
import com.jav.net.security.channel.joggle.ISecurityProxySender;
import com.jav.net.security.protocol.InitProtocol;
import com.jav.net.security.protocol.RequestProtocol;
import com.jav.net.security.protocol.TransProtocol;

import java.nio.ByteBuffer;

/**
 * 安全协议数据发送者
 *
 * @author yyz
 */
public class SecurityProxySender extends SecuritySender implements ISecurityProxySender {

    /**
     * 通道id
     */
    private String mChannelId;


    /**
     * 设置通道的id
     *
     * @param channelId 通道id
     */
    public void setChannelId(String channelId) {
        mChannelId = channelId;
    }


    /**
     * 客户端向服务端发起init协议请求
     *
     * @param machineId             机器id
     * @param initData              如果是des加密方式即是传密码,不是为null
     * @param changeEncryption      需要切换的加密方式
     * @param changeEncryptCallBack 切换加密方式回调
     */
    public void requestInitData(String machineId, byte[] initData, EncryptionType changeEncryption,
                                IChangeEncryptCallBack changeEncryptCallBack) {
        // 发送init协议数据
        InitProtocol initProtocol = new InitProtocol(machineId, initData);
        ByteBuffer encode = initProtocol.toData(mEncryptComponent);
        changeEncryptCallBack.onChange(changeEncryption);
        mCoreSender.sendData(new MultiByteBuffer(encode));
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
        InitProtocol initProtocol = new InitProtocol(machineId, repCode, initData);
        ByteBuffer encode = initProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encode));
    }


    /**
     * 响应 connect 请求
     *
     * @param requestId
     * @param result
     */
    @Override
    public void respondToRequest(String requestId, byte result) {
        if (requestId == null) {
            return;
        }
        RequestProtocol connectProtocol = new RequestProtocol(mChannelId);
        connectProtocol.setRequestId(requestId.getBytes());
        connectProtocol.updateSendData(new byte[]{result});
        ByteBuffer encodeData = connectProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
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
        RequestProtocol connectProtocol = new RequestProtocol(mChannelId);
        connectProtocol.setRequestId(requestId.getBytes());
        connectProtocol.setRequestAdr(address);
        ByteBuffer encodeData = connectProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
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
        TransProtocol transProtocol = new TransProtocol(mChannelId);
        transProtocol.updateSendData(data);
        transProtocol.setRequestId(requestId.getBytes());
        ByteBuffer encodeData = transProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
    }

    /**
     * 相应 trans 请求
     *
     * @param requestId 异常的情况可以返回任意32 bit长度的内容
     * @param repCode   异常返回1，正常返回0
     * @param data      异常情况可以返回null
     */
    @Override
    public void respondToTrans(String requestId, byte repCode, byte[] data) {
        TransProtocol transProtocol = new TransProtocol(mChannelId);
        transProtocol.setRepCode(repCode);
        transProtocol.updateSendData(data);
        transProtocol.setRequestId(requestId.getBytes());
        ByteBuffer encodeData = transProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
    }
}
