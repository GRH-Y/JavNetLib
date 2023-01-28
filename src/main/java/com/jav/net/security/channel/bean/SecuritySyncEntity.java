package com.jav.net.security.channel.bean;

/**
 * 同步服务负载数据
 *
 * @author yyz
 */
public class SecuritySyncEntity {

    /**
     * 目标代理服务的地址
     */
    private String mHost;

    /**
     * 目标代理服务的端口
     */
    private int mPort;

    /**
     * 目标机器的机器id
     */
    private String mMid;

    /**
     * 负载压力值
     */
    private long mLoadCount;

    public SecuritySyncEntity(String mid, String host) {
        mMid = mid;
        mHost = host;
    }

    public void updateLoadCount(long loadCount) {
        mLoadCount = loadCount;
    }

    public String getProxyHost() {
        return mHost;
    }

    public void setProxyPort(int port) {
        mPort = port;
    }

    public int getProxyPort() {
        return mPort;
    }

    public String getMid() {
        return mMid;
    }

    public long getLoadCount() {
        return mLoadCount;
    }
}
