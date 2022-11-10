package com.jav.net.security.channel;

import com.jav.common.cryption.AESDataEnvoy;
import com.jav.common.cryption.DataSafeManager;
import com.jav.common.cryption.RSADataEnvoy;
import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.entity.MultiByteBuffer;
import com.jav.net.nio.NioSender;
import com.jav.net.security.channel.joggle.ISecuritySender;
import com.jav.net.security.protocol.InitProtocol;
import com.jav.net.security.protocol.RequestProtocol;
import com.jav.net.security.protocol.TransProtocol;

import java.nio.ByteBuffer;

/**
 * 安全协议数据发送者
 *
 * @author yyz
 */
public class SecuritySender implements ISecuritySender {

    private TransProtocol mTransProtocol;
    private RequestProtocol mRequestProtocol;

    private final NioSender mCoreSender;
    private final DataSafeManager mDataSafeManager;


    public SecuritySender() {
        mDataSafeManager = new DataSafeManager();
        // 默认第一次是使用rsa加密
        mDataSafeManager.init(EncryptionType.RSA);
        IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
        RSADataEnvoy rsaDataEnvoy = encryptComponent.getComponent();
        String publicFile = SecurityChannelContext.getInstance().getPublicFile();
        String privateFile = SecurityChannelContext.getInstance().getPrivateFile();
        try {
            rsaDataEnvoy.init(publicFile, privateFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        mCoreSender = new NioSender();
    }

    public NioSender getCoreSender() {
        return mCoreSender;
    }

    /**
     * 客户端向服务端发起init协议请求
     *
     * @param machineId  机器id
     * @param encryption 加密类型
     */
    protected void requestInitData(String machineId, String encryption) {
        IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
        // 获取加密的方式
        EncryptionType configEncryptionType = EncryptionType.getInstance(encryption);

        byte[] initData = null;
        // 根据不同加密方式发送不同的数据
        if (configEncryptionType == EncryptionType.AES) {
            String desPassword = SecurityChannelContext.getInstance().getDesPassword();
            initData = desPassword.getBytes();
        }

        // 发送init协议数据
        InitProtocol initProtocol = new InitProtocol(machineId, initData);
        ByteBuffer encode = initProtocol.toData(encryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encode));
    }

    /**
     * 服务端向客户端响应init协议请求
     *
     * @param machineId 机器id
     * @param repCode   响应码
     * @param initData  数据
     */
    protected void respondInitData(String machineId, byte repCode, byte[] initData) {
        InitProtocol initProtocol = new InitProtocol(machineId, repCode, initData);
        IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
        ByteBuffer encode = initProtocol.toData(encryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encode));
    }

    /**
     * 设置通道的id
     *
     * @param channelId 通道id
     */
    protected void setChannelId(String channelId) {
        mRequestProtocol = new RequestProtocol(channelId);
        mTransProtocol = new TransProtocol(channelId);
    }


    /**
     * 切换加密方式
     *
     * @param encryption 加密方式
     * @param aesKey     ase的密钥
     */
    protected void changeEncryption(EncryptionType encryption, byte[] aesKey) {
        if (encryption == EncryptionType.BASE64) {
            mDataSafeManager.init(encryption);
        } else if (encryption == EncryptionType.AES) {
            // 获取AES对称密钥
            IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
            AESDataEnvoy encryptEnvoy = encryptComponent.getComponent();
            encryptEnvoy.initKey(aesKey);
        }
    }

    @Override
    public void senderRequest(String requestId, byte[] address) {
        if (requestId == null || address == null) {
            return;
        }
        synchronized (mCoreSender) {
            mRequestProtocol.setRequestId(requestId.getBytes());
            mRequestProtocol.setRequestAdr(address);
            IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
            ByteBuffer encodeData = mTransProtocol.toData(encryptComponent);
            mCoreSender.sendData(new MultiByteBuffer(encodeData));
        }
    }

    @Override
    public void senderTrans(String requestId, byte[] data) {
        if (requestId == null || data == null) {
            return;
        }
        synchronized (mDataSafeManager) {
            mTransProtocol.setRequestId(requestId.getBytes());
            mTransProtocol.updateSendData(data);
            IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
            ByteBuffer encodeData = mTransProtocol.toData(encryptComponent);
            mCoreSender.sendData(new MultiByteBuffer(encodeData));
        }
    }
}
