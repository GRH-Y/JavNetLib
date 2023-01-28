package com.jav.net.security.channel;


import com.jav.common.cryption.AESDataEnvoy;
import com.jav.common.cryption.DataSafeManager;
import com.jav.common.cryption.RSADataEnvoy;
import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.cryption.joggle.IDecryptComponent;
import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.base.SecurityPolicyProcessor;
import com.jav.net.security.channel.joggle.ChannelStatus;

/**
 * ChanelMeter 通道辅助，向外提供服务
 *
 * @author yyz
 */
public class SecurityChanelMeter {

    /**
     * 配置信息
     */
    protected final SecurityChannelContext mContext;

    /**
     * 通道当前的状态
     */
    private volatile ChannelStatus mCruStatus = ChannelStatus.NONE;

    /**
     * 数据安全管理，提供加解密
     */
    protected final DataSafeManager mDataSafeManager;

    /**
     * 协议解析器
     */
    private SecurityProtocolParser mProtocolParser;

    /**
     * 数据发送者
     */
    private SecuritySender mRealSender;

    /**
     * 数据接收者
     */
    private SecurityReceiver mRealReceiver;


    public SecurityChanelMeter(SecurityChannelContext context) {
        mContext = context;
        // 创建数据加密器管理器
        mDataSafeManager = new DataSafeManager();
        // 创建协议解析器
        mProtocolParser = new SecurityProtocolParser(mContext);
        // 创建安全策略处理器
        SecurityPolicyProcessor policyProcessor = new SecurityPolicyProcessor(mContext);
        // 设置安全策略处理器
        mProtocolParser.setSecurityPolicyProcessor(policyProcessor);
    }

    protected <T extends SecuritySender> T getSender() {
        return (T) mRealSender;
    }

    protected <T extends SecurityReceiver> T getReceiver() {
        return (T) mRealReceiver;
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
     * 获取当前通道状态
     *
     * @return 返回当前通道状态
     */
    protected ChannelStatus getCruStatus() {
        synchronized (mContext) {
            return mCruStatus;
        }
    }


    /**
     * 更新当前通道状态
     *
     * @param status 新的状态
     */
    protected void updateCurStatus(ChannelStatus status) {
        synchronized (mContext) {
            mCruStatus = status;
        }
    }

    protected void initEncryptionType() {
        // 初始化默认的加密方式
        mDataSafeManager.init(EncryptionType.RSA);
        IDecryptComponent decryptComponent = mDataSafeManager.getDecrypt();
        RSADataEnvoy decode = decryptComponent.getComponent();

        IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
        RSADataEnvoy encode = encryptComponent.getComponent();

        String publicFile = mContext.getInitRsaPublicFile();
        String privateFile = mContext.getInitRsaPrivateFile();
        try {
            decode.init(publicFile, privateFile);
            encode.init(publicFile, privateFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 切换加密方式
     *
     * @param encryption 加密方式
     */
    protected void changeEncryptionType(EncryptionType encryption) {
        // 发送完init数据,开始切换加密方式
        mDataSafeManager.init(encryption);
        IDecryptComponent decryptComponent = mDataSafeManager.getDecrypt();
        IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();
        if (encryption == EncryptionType.AES) {
            // 获取AES对称密钥
            String desPassword = mContext.getDesPassword();
            byte[] aesKey = desPassword.getBytes();
            AESDataEnvoy encryptEnvoy = encryptComponent.getComponent();
            encryptEnvoy.initKey(aesKey);
        }

        // 更新加密方式
        mRealSender.setEncryptComponent(encryptComponent);
        mRealReceiver.setDecryptComponent(decryptComponent);
    }


    /**
     * 扩展通道就绪回调
     */
    protected void onExtChannelReady() {
    }


    /**
     * 通道建立链接后回调
     *
     * @param sender   当前通道客户端的数据发送者
     * @param receiver 当前通道客户端的数据接收者
     */
    protected final void onChannelReady(SecurityChannelClient client, SecuritySender sender, SecurityReceiver receiver) {
        mRealSender = sender;
        mRealReceiver = receiver;

        initEncryptionType();

        IDecryptComponent decryptComponent = mDataSafeManager.getDecrypt();
        IEncryptComponent encryptComponent = mDataSafeManager.getEncrypt();

        // 设置协议解析器
        mRealReceiver.setProtocolParser(mProtocolParser);
        mRealReceiver.setDecryptComponent(decryptComponent);
        mRealSender.setEncryptComponent(encryptComponent);

        onExtChannelReady();

        client.onRegistrarReady();
    }


    /**
     * 通道失效回调
     */
    protected void onChannelInvalid() {
        updateCurStatus(ChannelStatus.INVALID);
    }


    /**
     * 通道断开自动重连接
     *
     * @param client
     */
    protected void onChannelReConnect(SecurityChannelClient client) {
        client.setAddress(mContext.getConnectHost(), mContext.getConnectPort());
        SecurityChannelManager.getInstance().resetChannel(client);
    }

}
