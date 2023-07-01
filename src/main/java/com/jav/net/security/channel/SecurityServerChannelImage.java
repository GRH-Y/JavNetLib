package com.jav.net.security.channel;

import com.jav.net.security.channel.joggle.IServerChannelStatusListener;
import com.jav.net.security.protocol.RequestProtocol;

/**
 * 服务模式的通道的镜像
 *
 * @author yyz
 */
public class SecurityServerChannelImage extends SecurityChannelImage {

    private IServerChannelStatusListener mListener;

    protected SecurityServerChannelImage(IServerChannelStatusListener listener) {
        mListener = listener;
    }

    public IServerChannelStatusListener getChannelStatusListener() {
        return mListener;
    }

    /**
     * 响应init请求
     *
     * @param machineId
     * @param repCode
     * @param initData
     */
    public void respondInitData(String machineId, byte repCode, byte[] initData) {
        mSender.respondToInitRequest(machineId, repCode, initData);
    }

    /**
     * 响应 connect 请求
     *
     * @param requestId
     * @param result
     */
    public void respondRequestData(String requestId, byte result) {
        mSender.respondToRequest(requestId, result);
    }


    /**
     * 发送需要中转的数据
     *
     * @param data 数据
     */
    public void sendTransDataFromServer(String requestId, byte[] data) {
        mSender.respondToTrans(requestId, RequestProtocol.REP_SUCCESS_CODE, data);
    }


    /**
     * 创建服务端的通道镜像
     *
     * @param listener 状态监听器
     * @return 通道镜像
     */
    public static SecurityServerChannelImage builderServerChannelImage(IServerChannelStatusListener listener) {
        return new SecurityServerChannelImage(listener);
    }
}
