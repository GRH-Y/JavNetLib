package com.jav.net.security.channel;


import com.jav.common.cryption.AESComponent;
import com.jav.common.cryption.DataSafeManager;
import com.jav.common.cryption.RSAComponent;
import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.cryption.joggle.ICipherComponent;
import com.jav.net.base.MultiBuffer;
import com.jav.net.security.channel.base.AbsSecurityMeter;
import com.jav.net.security.channel.base.ChannelEncryption;
import com.jav.net.security.channel.base.ChannelStatus;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.guard.SecurityPolicyProcessor;

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
    private final SecurityCommProtocolParser mProtocolParser;


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
     * @param encryption     加密方式
     */
    protected void configEncryptionMode(EncryptionType encryptionType, ChannelEncryption encryption) {
        // 发送完init数据,开始切换加密方式
        mDataSafeManager.init(encryptionType);
        if (encryptionType == EncryptionType.AES) {
            // 获取AES对称密钥
            ChannelEncryption.TransmitEncryption transmitEncryption = encryption.getTransmitEncryption();
            byte[] aesKey = transmitEncryption.getPassword();
            AESComponent aesComponent = mDataSafeManager.getCipherComponent();
            aesComponent.initKey(aesKey, null);
        } else if (encryptionType == EncryptionType.RSA) {
            ChannelEncryption.InitEncryption initEncryption = encryption.getInitEncryption();
            String publicFile = initEncryption.getPublicFile();
            String privateFile = initEncryption.getPrivateFile();
            try {
                RSAComponent rsaComponent = mDataSafeManager.getCipherComponent();
                rsaComponent.init(publicFile, privateFile);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 设置加密方式
        ICipherComponent cipherComponent = mDataSafeManager.getCipherComponent();
        mRealSender.setEncryptComponent(cipherComponent);
        mRealReceiver.setDecryptComponent(cipherComponent);
    }


    /**
     * 通道建立链接后回调
     *
     * @param sender   当前通道客户端的数据发送者
     * @param receiver 当前通道客户端的数据接收者
     */
    @Override
    protected void onConfigChannel(SecuritySender<MultiBuffer> sender, SecurityReceiver receiver) {
        super.onConfigChannel(sender, receiver);
        // 设置协议解析器
        mRealReceiver.setProtocolParser(mProtocolParser);

        //设置加密方式
        ChannelEncryption encryption = mContext.getChannelEncryption();
        EncryptionType encryptionType = encryption.getInitEncryption().getEncryptionType();
        configEncryptionMode(encryptionType, encryption);
    }


    @Override
    protected ChannelStatus getCruStatus() {
        return super.getCruStatus();
    }

    @Override
    protected void onChannelInvalid() {
        super.onChannelInvalid();
    }
}
