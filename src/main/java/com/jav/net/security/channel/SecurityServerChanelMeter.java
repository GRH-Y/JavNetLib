package com.jav.net.security.channel;


import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.log.LogDog;
import com.jav.common.security.Md5Helper;
import com.jav.net.security.cache.CacheChannelIdMater;
import com.jav.net.security.channel.base.ChannelStatus;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.base.UnusualBehaviorType;
import com.jav.net.security.channel.joggle.ISecurityChannelStatusListener;
import com.jav.net.security.channel.joggle.IServerChannelStatusListener;
import com.jav.net.security.channel.joggle.IServerEventCallBack;
import com.jav.net.security.protocol.base.InitResult;

import java.util.Map;
import java.util.UUID;

/**
 * ChanelMeter 通道辅助，向外提供服务
 *
 * @author yyz
 */
public class SecurityServerChanelMeter extends SecurityChanelMeter {

    /**
     * server通道镜像
     */
    private volatile SecurityServerChannelImage mServerImage;

    public SecurityServerChanelMeter(SecurityChannelContext context) {
        super(context);
        ReceiveProxy receiveProxy = new ReceiveProxy();
        ParserCallBackRegistrar registrar = new ParserCallBackRegistrar(receiveProxy);
        setProtocolParserCallBack(registrar);
    }

    /**
     * 接收分发数据
     */
    private class ReceiveProxy implements IServerEventCallBack {

        @Override
        public void onInitForServerCallBack(EncryptionType encryption, byte[] aesKey, String machineId) {
            // 根据客户端,切换加密方式
            changeEncryptionType(encryption);

            IServerChannelStatusListener supperListener = mServerImage.getChannelStatusListener();
            boolean intercept = supperListener.onRespondInitData(machineId);
            if (intercept) {
                return;
            }
            // 创建channel id 返回给客户端
            String channelId = createChannelId(machineId);
            // 配置channel id
            SecurityProxySender proxySender = getSender();
            proxySender.setChannelId(channelId);
            LogDog.d("创建channel id 返回给客户端 " + channelId);

            proxySender.respondToInitRequest(machineId, InitResult.CHANNEL_ID.getCode(), channelId.getBytes());
        }

        @Override
        public void onConnectTargetCallBack(String requestId, String realHost, int port) {
            // 分发请求创建目标链接数据
            if (mServerImage != null) {
                IServerChannelStatusListener supperListener = mServerImage.getChannelStatusListener();
                supperListener.onCreateConnect(requestId, realHost, port);
            }
        }


        @Override
        public void onTransData(String requestId, byte[] data) {
            // 分发中转数据
            if (mServerImage != null) {
                IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
                serverListener.onRequestTransData(requestId, data);
            }
        }

        @Override
        public void onChannelError(UnusualBehaviorType error, Map<String, String> extData) {
            if (mServerImage != null) {
                IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
                serverListener.onChannelError(error, extData);
            }
        }

    }

    /**
     * 创建通道id
     *
     * @return 返回通道id
     */
    private String createChannelId(String machineId) {
        String uuidStr = UUID.randomUUID().toString() + System.nanoTime();
        String channelId = Md5Helper.md5_32(uuidStr);
        CacheChannelIdMater.getInstance().binderChannelIdToMid(machineId, channelId);
        return channelId;
    }


    /**
     * 注册服务端通道镜像
     *
     * @param image 通道镜像
     */
    public void regServerChannelImage(SecurityServerChannelImage image) {
        if (getCruStatus() == ChannelStatus.INVALID || !mContext.isServerMode() || mServerImage != null) {
            return;
        }
        mServerImage = image;
        SecurityProxySender proxySender = getSender();
        mServerImage.setProxySender(proxySender);

        if (getCruStatus() == ChannelStatus.READY) {
            mServerImage.updateStatus(getCruStatus());
            ISecurityChannelStatusListener listener = mServerImage.getChannelStatusListener();
            listener.onChannelReady(mServerImage);
        }
    }


    /**
     * 反注册通道
     *
     * @param image 通道镜像
     * @return true 为反注册成功
     */
    public boolean unRegServerChannelImage(SecurityServerChannelImage image) {
        if (mServerImage == image) {
            mServerImage = null;
            return true;
        }
        return false;
    }


    @Override
    protected void onExtChannelReady() {
        updateCurStatus(ChannelStatus.READY);
        if (mServerImage != null) {
            mServerImage.updateStatus(getCruStatus());
            ISecurityChannelStatusListener listener = mServerImage.getChannelStatusListener();
            listener.onChannelReady(mServerImage);
        }
    }


    /**
     * 通道失效回调
     */
    protected void onChannelInvalid() {
        super.onChannelInvalid();
        if (mServerImage != null) {
            mServerImage.updateStatus(getCruStatus());
            ISecurityChannelStatusListener listener = mServerImage.getChannelStatusListener();
            listener.onChannelInvalid();
        }
    }

}
