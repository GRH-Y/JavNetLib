package com.jav.net.svp.channel;

public class SvpChannelContext {
    private final Builder mBuilder;

    private SvpChannelContext(Builder builder) {
        mBuilder = builder;
    }

    public boolean isServerMode() {
        return mBuilder.mIsServerMode;
    }

    public String getHost() {
        return mBuilder.mHost;
    }

    public int getPort() {
        return mBuilder.mPort;
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

        public Builder configAddress(String host, int port) {
            this.mHost = host;
            this.mPort = port;
            return this;
        }

        /**
         * 初始化为服务端
         *
         * @return
         */
        public SvpChannelContext asServer() {
            this.mIsServerMode = true;

            return builder();
        }

        /**
         * 初始化为客户端
         *
         * @return
         */
        public SvpChannelContext asClient() {
            this.mIsServerMode = false;

            return builder();
        }

        private SvpChannelContext builder() {
            return new SvpChannelContext(this);
        }
    }
}
