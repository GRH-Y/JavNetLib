package com.jav.net.security.channel;


import com.jav.common.log.LogDog;
import com.jav.common.security.Md5Helper;
import com.jav.net.security.channel.joggle.IClientChannelStatusListener;

import java.util.UUID;

/**
 * ChannelImage 通道的镜像,一个通道多方共享使用
 *
 * @author yyz
 */
public class SecurityClientChannelImage extends SecurityChannelImage {

    /**
     * 请求id，由于通道是共享用于区别请求者
     */
    protected String mRequestId;

    /**
     * 通道状态监听器
     */
    private final IClientChannelStatusListener mListener;

    protected SecurityClientChannelImage(String requestId, IClientChannelStatusListener listener) {
        if (listener == null) {
            throw new RuntimeException("listener can not be null !!!");
        }
        mListener = listener;
        mRequestId = requestId;
    }


    /**
     * 获取通道状态监听器
     *
     * @return 返回状态监听器
     */
    protected IClientChannelStatusListener getChannelStatusListener() {
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
     * 发送需要中转的数据
     *
     * @param host 地址
     * @param port 端口
     */
    public void sendRequestDataFromClinet(String host, int port) {
        String addressStr = host + ":" + port;
        mSender.sendRequestData(mRequestId, addressStr.getBytes());
    }

    /**
     * 发送需要中转的数据
     *
     * @param data 数据
     */
    public void sendTransDataFromClinet(byte[] data) {
        mSender.sendTransData(mRequestId, data);
    }


    /**
     * 创建客户端的通道镜像
     *
     * @param listener 状态监听器
     * @return 通道镜像
     */
    protected static SecurityClientChannelImage builderClientChannelImage(IClientChannelStatusListener listener) {
        String uuidStr = UUID.randomUUID().toString();
        String requestId = Md5Helper.md5_32(uuidStr);
        LogDog.d("#SC# builderClientChannelImage requestId = " + requestId);
        return new SecurityClientChannelImage(requestId, listener);
    }

}
