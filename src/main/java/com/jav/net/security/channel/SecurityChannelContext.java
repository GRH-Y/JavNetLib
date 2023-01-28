package com.jav.net.security.channel;

import com.jav.net.security.channel.base.AbsSecurityServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 安全通道的上下文同时也是构建器
 *
 * @author yyz
 */
public class SecurityChannelContext {

    /**
     * 最大的通道数
     */
    private static final int MAX_CHANNEL = 2;

    private Builder mBuilder;

    private SecuritySyncMeter mSyncMeter;


    private SecurityChannelContext(Builder builder) {
        mBuilder = builder;
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

    public String getInitRsaPublicFile() {
        return mBuilder.mPublicFile;
    }

    public String getInitRsaPrivateFile() {
        return mBuilder.mPrivateFile;
    }

    public String getDesPassword() {
        return mBuilder.mDesPassword;
    }

    public List<String> getMachineList() {
        return mBuilder.mMachineList;
    }

    public String getConnectHost() {
        return mBuilder.mHost;
    }

    public int getConnectPort() {
        return mBuilder.mPort;
    }

    public int getChannelNumber() {
        return mBuilder.mChannelNumber;
    }

    protected List<AbsSecurityServer> getSecurityServer() {
        return mBuilder.mServerList;
    }

    public boolean isEnableIpBlack() {
        return mBuilder.mEnableIPBlack;
    }

    protected void resetConnectSecurityServer(String host, int port) {
        mBuilder.mHost = host;
        mBuilder.mPort = port;
    }

    protected void setSyncMeter(SecuritySyncMeter meter) {
        mSyncMeter = meter;
    }

    public SecuritySyncMeter getSyncMeter() {
        return mSyncMeter;
    }

    protected Map<String, String> getSyncServer() {
        return mBuilder.mSyncServer;
    }

    protected String getSyncHost() {
        return mBuilder.mLocalSyncHost;
    }

    protected int getSyncPort() {
        return mBuilder.mLocalSyncPort;
    }

    public static class Builder {
        /**
         * 是否服务端模式
         */
        private boolean mIsServerMode;

        /**
         * 链接服务的地址
         */
        private String mHost;

        /**
         * 链接服务的端口
         */
        private int mPort;

        /**
         * 通道数量
         */
        private int mChannelNumber;

        /**
         * 开启的服务端列表
         */
        private List<AbsSecurityServer> mServerList;

        /**
         * 是否开启拦截异常的ip功能
         */
        private boolean mEnableIPBlack;

        /**
         * 机器id
         */
        private String mMachineId;

        /**
         * 加密的方式
         */
        private String mEncryption;

        /**
         * rsa公钥文件路径
         */
        private String mPublicFile;

        /**
         * rsa私钥文件路径
         */
        private String mPrivateFile;

        /**
         * 同步服务列表
         */
        private Map<String, String> mSyncServer;

        private String mLocalSyncHost;

        private int mLocalSyncPort;

        /**
         * des密码
         */
        private String mDesPassword;

        /**
         * 机器id列表
         */
        private List<String> mMachineList;

        public Builder configConnectSecurityServer(String host, int port) {
            this.mHost = host;
            this.mPort = port;
            return this;
        }

        public Builder configBootSecurityServer(AbsSecurityServer server) {
            if (mServerList == null) {
                mServerList = new ArrayList<>();
            }
            mServerList.add(server);
            return this;
        }

        public Builder setChannelNumber(int channelNumber) {
            this.mChannelNumber = channelNumber;
            if (mChannelNumber <= 0) {
                this.mChannelNumber = MAX_CHANNEL;
            }
            return this;
        }

        public Builder setIpBlackStatus(boolean isEnable) {
            this.mEnableIPBlack = isEnable;
            return this;
        }

        public Builder setServerMode(boolean isServerMode) {
            this.mIsServerMode = isServerMode;
            return this;
        }

        public Builder setMachineId(String machineId) {
            this.mMachineId = machineId;
            return this;
        }

        public Builder setEncryption(String encryption, String password) {
            this.mEncryption = encryption;
            this.mDesPassword = password;
            return this;
        }

        public Builder setInitRsaKeyFile(String publicFile, String privateFile) {
            this.mPublicFile = publicFile;
            this.mPrivateFile = privateFile;
            return this;
        }

        public Builder setMachineList(List<String> machineList) {
            this.mMachineList = machineList;
            return this;
        }

        public Builder setSyncServer(Map<String, String> syncServer) {
            this.mSyncServer = syncServer;
            return this;
        }

        public Builder setLocalSyncInfo(String syncHost, int syncPort) {
            this.mLocalSyncHost = syncHost;
            this.mLocalSyncPort = syncPort;
            return this;
        }

        public SecurityChannelContext builder() {
            return new SecurityChannelContext(this);
        }
    }
}