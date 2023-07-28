package com.jav.net.security.channel;


import com.jav.common.cryption.AESDataEnvoy;
import com.jav.common.cryption.DataSafeManager;
import com.jav.common.cryption.RSADataEnvoy;
import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.cryption.joggle.IDecryptComponent;
import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.base.AbsSecurityMeter;
import com.jav.net.security.channel.base.ChannelStatus;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.base.SecurityPolicyProcessor;
import com.jav.net.security.channel.joggle.ChannelEncryption;

/**
 * ChanelMeter 通道辅助，向外提供服务
 *
 * @author yyz
 */
public class SecurityChanelMeter extends AbsSecurityMeter {

    /**
     * 配置信息
     */
    protected final SecurityChannelContext mContext;


    /**
     * 数据安全管理，提供加解密
     */
    private final DataSafeManager mDataSafeManager;

    /**
     * 协议解析器
     */
    private SecurityCommProtocolParser mProtocolParser;


    public SecurityChanelMeter(SecurityChannelContext context) {
        mContext = context;
        // 创建数据加密器管理器
        mDataSafeManager = new DataSafeManager();
        // 创建协议解析器
        mProtocolParser = new SecurityCommProtocolParser(context);
        // 创建安全策略处理器
        SecurityPolicyProcessor policyProcessor = new SecurityPolicyProcessor(mContext);
        // 设置安全策略处理器
        mProtocolParser.setSecurityPolicyProcessor(policyProcessor);
    }


    /**
     * 设置协议解析回调对象
     *
     * @param registrar
     */
    protected void setProtocolParserCallBack(ParserCallBackRegistrar registrar) {
        mProtocolParser.setProtocolParserCallBack(registrar);
    }


    /**
     * 配置加密方式
     *
     * @param encryptionType 加密方式
     * @param encryptionType 加密方式
     */
    protected void configEncryptionMode(EncryptionType encryptionType, ChannelEncryption encryption) {
        // 发送完init数据,开始切换加密方式
        mDataSafeManager.init(encryptionType);
        IDecryptComponent decryptComponent = mDataSafeManager.getDecrypt();
        IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
        if (encryptionType == EncryptionType.AES) {
            // 获取AES对称密钥
            ChannelEncryption.TransmitEncryption transmitEncryption = encryption.getTransmitEncryption();
            String desPassword = transmitEncryption.getPassword();
            byte[] aesKey = desPassword.getBytes();
            AESDataEnvoy encryptEnvoy = encryptComponent.getComponent();
            encryptEnvoy.initKey(aesKey);
        } else if (encryptionType == EncryptionType.RSA) {
            ChannelEncryption.InitEncryption initEncryption = encryption.getInitEncryption();
            String publicFile = initEncryption.getPublicFile();
            String privateFile = initEncryption.getPrivateFile();
            try {
                RSADataEnvoy decode = decryptComponent.getComponent();
                RSADataEnvoy encode = encryptComponent.getComponent();
                decode.init(publicFile, privateFile);
                encode.init(publicFile, privateFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 设置加密方式
        mRealSender.setEncryptComponent(encryptComponent);
        mRealReceiver.setDecryptComponent(decryptComponent);
    }


    /**
     * 通道建立链接后回调
     *
     * @param sender   当前通道客户端的数据发送者
     * @param receiver 当前通道客户端的数据接收者
     */
    @Override
    protected void onChannelReady(SecuritySender sender, SecurityReceiver receiver) {
        super.onChannelReady(sender, receiver);

        //设置加密方式
        ChannelEncryption encryption = mContext.getChannelEncryption();
        EncryptionType encryptionType = encryption.getInitEncryption().getEncryptionType();
        configEncryptionMode(encryptionType, encryption);
        // 设置协议解析器
        mRealReceiver.setProtocolParser(mProtocolParser);
        //重置接收,断线重连接需要恢复状态
        mRealReceiver.reset();
        //回调chanel准备就绪
        onExtChannelReady();
    }

    @Override
    protected ChannelStatus getCruStatus() {
        return super.getCruStatus();
    }

    @Override
    protected void updateCurStatus(ChannelStatus status) {
        super.updateCurStatus(status);
    }

    @Override
    protected void onExtChannelReady() {
        super.onExtChannelReady();
    }

    @Override
    protected void onChannelInvalid() {
        super.onChannelInvalid();
    }
}
