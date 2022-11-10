package com.jav.net.security.channel;

import java.util.List;

/**
 * 安全通道的上下文
 *
 * @author yyz
 */
public class SecurityChannelContext {

    private Builder mBuilder = null;

    private static class InnerClass {
        private final static SecurityChannelContext sContext = new SecurityChannelContext();
    }

    public static SecurityChannelContext getInstance() {
        return InnerClass.sContext;
    }

    public static SecurityChannelContext init(Builder builder) {
        return InnerClass.sContext.setBuilder(builder);
    }

    private SecurityChannelContext() {
    }

    private SecurityChannelContext setBuilder(Builder builder) {
        this.mBuilder = builder;
        return this;
    }

    public boolean isServerMode() {
        return mBuilder.mIsServerMode;
    }

    public String getMachineId() {
        return mBuilder.mMachineId;
    }

    public String getEncryption() {
        return mBuilder.mEncryption;
    }

    public String getPublicFile() {
        return mBuilder.mPublicFile;
    }

    public String getPrivateFile() {
        return mBuilder.mPrivateFile;
    }

    public String getDesPassword() {
        return mBuilder.mDesPassword;
    }

    public List<String> getMachineList() {
        return mBuilder.mMachineList;
    }

    public static class Builder {
        //是否服务端模式
        private boolean mIsServerMode;

        //机器id
        private String mMachineId;

        //加密的方式
        private String mEncryption;

        //rsa公钥文件路径
        private String mPublicFile;

        //rsa私钥文件路径
        private String mPrivateFile;

        //des密码
        private String mDesPassword;

        //机器id列表
        private List<String> mMachineList;

        public Builder setServerMode(boolean isServerMode) {
            this.mIsServerMode = isServerMode;
            return this;
        }

        public Builder setMachineId(String machineId) {
            this.mMachineId = machineId;
            return this;
        }

        public Builder setEncryption(String encryption) {
            this.mEncryption = encryption;
            return this;
        }

        public Builder setPublicFile(String publicFile) {
            this.mPublicFile = publicFile;
            return this;
        }

        public Builder setPrivateFile(String privateFile) {
            this.mPrivateFile = privateFile;
            return this;
        }

        public Builder setDesPassword(String desPassword) {
            this.mDesPassword = desPassword;
            return this;
        }

        public Builder setMachineList(List<String> machineList) {
            this.mMachineList = machineList;
            return this;
        }
    }
}