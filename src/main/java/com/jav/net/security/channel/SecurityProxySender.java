package com.jav.net.security.channel;

import com.jav.net.base.MultiBuffer;
import com.jav.net.security.channel.base.ConstantCode;
import com.jav.net.security.channel.joggle.ChannelEncryption;
import com.jav.net.security.channel.joggle.IChangeEncryptCallBack;
import com.jav.net.security.channel.joggle.ISecurityProxySender;
import com.jav.net.security.protocol.InitProtocol;
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
     * 返回channelId
     *
     * @return
     */
    public String getChannelId() {
        return mChannelId;
    }

    /**
     * 客户端向服务端发起init协议请求
     *
     * @param machineId             机器id
     * @param initData              如果是des加密方式即是传密码,不是为null
     * @param changeEncryption      需要切换的加密方式
     * @param changeEncryptCallBack 切换加密方式回调
     */
    public void requestInitData(String machineId, byte[] initData, ChannelEncryption changeEncryption,
                                IChangeEncryptCallBack changeEncryptCallBack) {
        // 发送init协议数据
        InitProtocol initProtocol = new InitProtocol(machineId);
        initProtocol.setSendData(initData);
        initProtocol.setOperateCode(EncodeCode.BASE64.getType());
        ByteBuffer encode = initProtocol.toData(mEncryptComponent);
        changeEncryptCallBack.onChange(changeEncryption);
        mCoreSender.sendData(new MultiBuffer(encode));
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
        ByteBuffer encode = initProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encode));
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
        TransProtocol connectProtocol = new TransProtocol(mChannelId, requestId);
        byte operateCode = (byte) (status | TransOperateCode.ADDRESS.getCode());
        connectProtocol.setOperateCode(operateCode);
        ByteBuffer encodeData = connectProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
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
        TransProtocol connectProtocol = new TransProtocol(mChannelId, requestId);
        connectProtocol.setOperateCode(TransOperateCode.ADDRESS.getCode());
        connectProtocol.setSendData(address);
        ByteBuffer encodeData = connectProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
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
        TransProtocol transProtocol = new TransProtocol(mChannelId, requestId);
        transProtocol.setOperateCode(TransOperateCode.DATA.getCode());
        transProtocol.setSendData(data);
        ByteBuffer encodeData = transProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
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
        TransProtocol transProtocol = new TransProtocol(mChannelId, requestId);
        byte operateCode = (byte) (status | TransOperateCode.DATA.getCode());
        transProtocol.setOperateCode(operateCode);
        transProtocol.setSendData(data);
        ByteBuffer encodeData = transProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiBuffer(encodeData));
    }
}
