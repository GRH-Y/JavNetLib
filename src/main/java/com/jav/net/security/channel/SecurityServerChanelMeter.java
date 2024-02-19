package com.jav.net.security.channel;


import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.log.LogDog;
import com.jav.net.base.MultiBuffer;
import com.jav.net.security.channel.base.*;
import com.jav.net.security.channel.joggle.IInitRespondResultCallBack;
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
    private final SecurityServerChannelImage mServerImage;


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
        public void onInitForServerCallBack(ChannelEncryption encryption, String machineId) {
            //设置machineId
            SecurityProxySender proxySender = getSender();
            proxySender.setMachineId(machineId);

            IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
            InitRespondResult initRespondResult = new InitRespondResult(encryption, new IInitRespondResultCallBack() {
                @Override
                public void onInitRespondResult(boolean intercept, ChannelEncryption encryption) {
                    if (intercept) {
                        return;
                    }
                    // 返回成功结果给客户端
                    proxySender.respondToInitRequest(InitResult.OK.getCode(), null);

                    // 根据客户端,切换加密方式
                    EncryptionType encryptionType = encryption.getTransmitEncryption().getEncryptionType();
                    configEncryptionMode(encryptionType, encryption);

                    SecurityMachineIdMonitor.getInstance().addRepeatMachineListener(machineId, serverListener);
                    LogDog.i("#SC# return ok to client = " + machineId);

                    updateCurStatus(ChannelStatus.READY);
                }
            });
            serverListener.onRespondInitData(machineId, initRespondResult);
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
            //更新镜像的状态
            mServerImage.updateStatus(newStatus);
            //通知镜像可用回调
            IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
            serverListener.onChannelImageReady(mServerImage);
        }
    }

    @Override
    protected void onConfigChannel(SecuritySender<MultiBuffer> sender, SecurityReceiver receiver) {
        super.onConfigChannel(sender, receiver);
        //配置代理sender给镜像
        SecurityProxySender proxySender = getSender();
        mServerImage.setProxySender(proxySender);
    }

    /**
     * 通道失效回调
     */
    @Override
    protected void onChannelInvalid() {
        super.onChannelInvalid();
        mServerImage.updateStatus(getCruStatus());
        IServerChannelStatusListener serverListener = mServerImage.getChannelStatusListener();
        serverListener.onChannelInvalid();
        SecurityProxySender proxySender = getSender();
        String machineId = proxySender.getMachineId();
        SecurityMachineIdMonitor.getInstance().removeRepeatMachineListener(machineId, serverListener);
    }

}
