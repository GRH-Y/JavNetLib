package com.jav.net.security.channel;


import com.jav.common.security.Md5Helper;
import com.jav.net.security.channel.joggle.*;

import java.util.UUID;

/**
 * ChannelImage 通道的镜像,一个通道多方共享使用
 *
 * @author yyz
 */
public class SecurityChannelImage {

    /**
     * 请求id，由于通道是共享用于区别请求者
     */
    private String mRequestId;

    /**
     * 通道状态监听器
     */
    private ChannelStatus mCruStatus;

    /**
     * 通道状态监听器
     */
    private final ISecurityChannelStatusListener mListener;

    /**
     * 发送者的代理接口
     */
    private ISecuritySender mSender;


    private SecurityChannelImage(String requestId, ISecurityChannelStatusListener listener) {
        mListener = listener;
        mRequestId = requestId;
    }

    /**
     * 设置发送者的代理接口
     *
     * @param sender 代理接口
     */
    protected void setProxySender(ISecuritySender sender) {
        this.mSender = sender;
    }

    /**
     * 回调通道当前状态
     *
     * @param status 状态
     */
    protected void onUpdateStatus(ChannelStatus status) {
        synchronized (this) {
            this.mCruStatus = status;
        }
    }

    /**
     * 获取通道状态监听器
     *
     * @return 返回状态监听器
     */
    protected ISecurityChannelStatusListener getChannelStatusListener() {
        return mListener;
    }


    /**
     * 获取当前镜像通道的请求id
     *
     * @return 请求id，区分是哪个镜像通道
     */
    public String getRequestId() {
        return mRequestId;
    }

    /**
     * 获取当前通道的状态
     *
     * @return 通道状态
     */
    public ChannelStatus getCruStatus() {
        synchronized (this) {
            return mCruStatus;
        }
    }


    /**
     * 发送需要中转的数据
     *
     * @param host 地址
     * @param port 端口
     */
    public void sendRequestDataFromClinet(String host, int port) {
        String addressStr = host + ":" + port;
        mSender.senderRequest(mRequestId, addressStr.getBytes());
    }

    /**
     * 发送需要中转的数据
     *
     * @param data 数据
     */
    public void sendTransDataFromClinet(byte[] data) {
        mSender.senderTrans(mRequestId, data);
    }

    /**
     * 发送需要中转的数据
     *
     * @param data 数据
     */
    public void sendTransDataFromServer(String requestId, byte[] data) {
        mSender.senderTrans(requestId, data);
    }


    /**
     * 创建客户端的通道镜像
     *
     * @param listener 状态监听器
     * @return 通道镜像
     */
    public static SecurityChannelImage builderClientChannelImage(IClientChannelStatusListener listener) {
        String uuidStr = UUID.randomUUID().toString();
        String requestId = Md5Helper.md5_32(uuidStr);
        return new SecurityChannelImage(requestId, listener);
    }

    /**
     * 创建服务端的通道镜像
     *
     * @param listener 状态监听器
     * @return 通道镜像
     */
    public static SecurityChannelImage builderServerChannelImage(IServerChannelStatusListener listener) {
        return new SecurityChannelImage(null, listener);
    }
}
