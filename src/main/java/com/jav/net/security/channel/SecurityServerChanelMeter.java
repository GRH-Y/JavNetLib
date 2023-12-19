package com.jav.net.security.channel;


import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.log.LogDog;
import com.jav.net.security.channel.base.ChannelStatus;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.base.UnusualBehaviorType;
import com.jav.net.security.channel.joggle.ChannelEncryption;
import com.jav.net.security.channel.joggle.ISecurityChannelStatusListener;
import com.jav.net.security.channel.joggle.IServerChannelStatusListener;
import com.jav.net.security.channel.joggle.IServerEventCallBack;
import com.jav.net.security.guard.SecurityMachineIdMonitor;
import com.jav.net.security.protocol.base.InitResult;

import java.util.Map;

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


    public SecurityServerChanelMeter(SecurityChannelContext context, SecurityServerChannelImage image) {
        super(context);
        mServerImage = image;
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
            //设置machineId
            SecurityProxySender proxySender = getSender();
            proxySender.setMachineId(machineId);

            // 根据客户端,切换加密方式
            ChannelEncryption channelEncryption = mContext.getChannelEncryption();
            configEncryptionMode(encryption, channelEncryption);

            IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
            boolean intercept = serverListener.onRespondInitData(machineId);
            if (intercept) {
                return;
            }
            // 返回成功结果给客户端
            proxySender.respondToInitRequest(machineId, InitResult.OK.getCode(), null);

            SecurityMachineIdMonitor.getInstance().setRepeatMachineListener(machineId, serverListener);
            LogDog.i("#SC# return ok to client = " + machineId);
        }

        @Override
        public void onConnectTargetCallBack(String requestId, String realHost, int port) {
            // 分发请求创建目标链接数据
            IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
            serverListener.onCreateConnect(requestId, realHost, port);
        }

        @Override
        public void onKeepAliveCallBack(String machineId) {
            mServerImage.sendKeepAlive(machineId);
        }


        @Override
        public void onTransData(String requestId, byte[] data) {
            // 分发中转数据
            IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
            serverListener.onRequestTransData(requestId, data);
        }

        @Override
        public void onChannelError(UnusualBehaviorType error, Map<String, String> extData) {
            IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
            serverListener.onChannelError(error, extData);
        }

    }


    @Override
    protected void onChannelChangeStatus(ChannelStatus newStatus) {
        if (newStatus.getCode() == ChannelStatus.READY.getCode()) {
            //配置代理sender给镜像
            SecurityProxySender proxySender = getSender();
            mServerImage.setProxySender(proxySender);
            //更新镜像的状态
            mServerImage.updateStatus(newStatus);
            //通知镜像可用回调
            ISecurityChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
            serverListener.onChannelImageReady(mServerImage);
        }
    }

    /**
     * 通道失效回调
     */
    @Override
    protected void onChannelInvalid() {
        super.onChannelInvalid();
        mServerImage.updateStatus(getCruStatus());
        ISecurityChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
        serverListener.onChannelInvalid();
    }

}
