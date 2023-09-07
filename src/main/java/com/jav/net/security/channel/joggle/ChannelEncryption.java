package com.jav.net.security.channel.joggle;

import com.jav.common.cryption.joggle.EncryptionType;

/**
 * 通道加密
 */
public class ChannelEncryption {


    private final Builder mBuilder;


    public static class InitEncryption {

        private final EncryptionType mEncryptionType;
        private final String mPublicFile;
        private final String mPrivateFile;

        private InitEncryption(String publicFile, String privateFile) {
            mPublicFile = publicFile;
            mPrivateFile = privateFile;
            mEncryptionType = EncryptionType.RSA;
        }

        public String getPrivateFile() {
            return mPrivateFile;
        }

        public String getPublicFile() {
            return mPublicFile;
        }

        public EncryptionType getEncryptionType() {
            return mEncryptionType;
        }
    }

    public static class TransmitEncryption {

        private String mPassword;
        private final EncryptionType mEncryptionType;

        private TransmitEncryption(EncryptionType encryptionType, String password) {
            mPassword = password;
            mEncryptionType = encryptionType;
        }


        public EncryptionType getEncryptionType() {
            return mEncryptionType;
        }

        public String getPassword() {
            return mPassword;
        }

    }

    private ChannelEncryption(Builder builder) {
        mBuilder = builder;
    }

    public InitEncryption getInitEncryption() {
        return mBuilder.mInitEncryption;
    }

    public TransmitEncryption getTransmitEncryption() {
        return mBuilder.mTransmitEncryption;
    }

    /**
     * 构造器
     */
    public static class Builder {

        private InitEncryption mInitEncryption;
        private TransmitEncryption mTransmitEncryption;

        /**
         * 初始化协议用到的RSA加密，必须跟服务端跟客户端是配套，不然不能进行通讯
         *
         * @param publicFile  RSA加密公钥文件
         * @param privateFile RSA加密私钥文件
         * @return
         */
        public Builder configInitEncryption(String publicFile, String privateFile) {
            if (publicFile == null || privateFile == null) {
                throw new IllegalArgumentException("publicFile or privateFile cannot be null !");
            }
            mInitEncryption = new InitEncryption(publicFile, privateFile);
            return this;
        }

        private void check() {
            if (mInitEncryption == null) {
                throw new IllegalArgumentException("The configInitRSA method was not called !");
            }
        }

        public ChannelEncryption builderAES(String password) {
            check();
            mTransmitEncryption = new TransmitEncryption(EncryptionType.AES, password);
            mTransmitEncryption.mPassword = password;
            return new com.jav.net.security.channel.joggle.ChannelEncryption(this);
        }

        public ChannelEncryption builderBase64() {
            check();
            mTransmitEncryption = new TransmitEncryption(EncryptionType.BASE64, null);
            return new ChannelEncryption(this);
        }

    }

}
