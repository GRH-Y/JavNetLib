package com.jav.net.security.channel;

import com.jav.net.security.channel.base.AbsSecurityServer;
import com.jav.net.security.channel.joggle.ChannelEncryption;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全通道的上下文同时也是构建器
 *
 * @author yyz
 */
public class SecurityChannelContext {

    /**
     * 最少的通道数
     */
    private static final int MIN_CHANNEL = 1;

    /**
     * 最大的通道数
     */
    public static final int MAX_CHANNEL = 4;

    private final Builder mBuilder;


    private SecurityChannelContext(Builder builder) {
        mBuilder = builder;
    }

    public boolean isServerMode() {
        return mBuilder.mIsServerMode;
    }

    public String getMachineId() {
        return mBuilder.mMachineId;
    }

    public ChannelEncryption getChannelEncryption() {
        return mBuilder.mEncryption;
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
        private ChannelEncryption mEncryption;


        /**
         * 机器id列表
         */
        private List<String> mMachineList;

        public Builder configConnectSecurityServer(String host, int port) {
            this.mHost = host;
            this.mPort = port;
            return this;
        }

        public Builder addSecurityServerStarter(AbsSecurityServer server) {
            if (mServerList == null) {
                mServerList = new ArrayList<>();
            }
            mServerList.add(server);
            return this;
        }

        public Builder setChannelNumber(int channelNumber) {
            this.mChannelNumber = channelNumber;
            if (mChannelNumber <= MIN_CHANNEL) {
                this.mChannelNumber = MIN_CHANNEL;
            }
            if (mChannelNumber >= MAX_CHANNEL) {
                this.mChannelNumber = MAX_CHANNEL;
            }
            return this;
        }

        public Builder setIpBlackStatus(boolean isEnable) {
            this.mEnableIPBlack = isEnable;
            return this;
        }


        public Builder setMachineId(String machineId) {
            this.mMachineId = machineId;
            return this;
        }


        public Builder setMachineList(List<String> machineList) {
            this.mMachineList = machineList;
            return this;
        }

        /**
         * 初始化为服务端
         *
         * @param encryption 加密方式
         * @return
         */
        public SecurityChannelContext asServer(ChannelEncryption encryption) {
            this.mIsServerMode = true;
            this.mEncryption = encryption;
            return builder();
        }

        /**
         * 初始化为客户端
         *
         * @param encryption 加密方式
         * @return
         */
        public SecurityChannelContext asClient(ChannelEncryption encryption) {
            this.mIsServerMode = false;
            this.mEncryption = encryption;
            return builder();
        }

        private SecurityChannelContext builder() {
            return new SecurityChannelContext(this);
        }
    }
}