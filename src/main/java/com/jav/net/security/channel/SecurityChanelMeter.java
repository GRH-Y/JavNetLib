package com.jav.net.security.channel;


import com.jav.common.security.Md5Helper;
import com.jav.net.security.channel.joggle.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ChanelMeter 通道辅助，向外提供服务
 *
 * @author yyz
 */
public class SecurityChanelMeter {

    /**
     * 通道当前的状态
     */
    private ChannelStatus mCruStatus = ChannelStatus.NONE;

    /**
     * 通道镜像集合
     */
    private final List<SecurityChannelImage> mChannelImageList;

    /**
     * 处理分发中转数据
     */
    private final ReceiveProxy mReceiveTransData;

    /**
     * 发送中转数据
     */
    private final SenderProxy mSendTransData;

    /**
     * 安全协议发送者
     */
    private SecuritySender mSender;
//    private SecurityReceiver mReceiver;


    /**
     * 获取当前通道状态
     *
     * @return 返回当前通道状态
     */
    public ChannelStatus getCruStatus() {
        return mCruStatus;
    }

    public SecurityChanelMeter() {
        mChannelImageList = new ArrayList<>();
        mReceiveTransData = new ReceiveProxy();
        mSendTransData = new SenderProxy();
    }

    /**
     * 接收分发数据
     */
    private class ReceiveProxy implements IReceiverTransDataProxy {

        @Override
        public void onCreateConnect(String requestId, String realHost, int port) {
            //分发请求创建目标链接数据
            notifyCreateData(requestId, realHost, port);
        }

        @Override
        public void onCreateConnectStatus(String requestId, byte status) {
            //分发请求创建目标链接结果回调
            notifyCreateConnectStatus(requestId, status);
        }

        @Override
        public void onTransData(String requestId, byte pctCount, byte[] data) {
            //分发中转数据
            notifyTransData(requestId, pctCount, data);
        }
    }

    /**
     * 发送中转数据
     */
    private class SenderProxy implements ISecuritySender {


        @Override
        public void senderRequest(String requestId, byte[] address) {
            mSender.senderRequest(requestId, address);
        }

        @Override
        public void senderTrans(String requestId, byte[] data) {
            //发送中转数据
            mSender.senderTrans(requestId, data);
        }
    }


    /**
     * 根据通道状态监听器查找通道镜像
     *
     * @param listener 监听器
     * @return 通道镜像
     */
    protected SecurityChannelImage findListenerToImage(IClientChannelStatusListener listener) {
        synchronized (mChannelImageList) {
            for (SecurityChannelImage image : mChannelImageList) {
                if (image.getChannelStatusListener() == listener) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * 注册通道镜像，获得通道的状态，发送数据，接收数据的能力
     *
     * @param image 通道镜像
     */
    public void regChannelImage(SecurityChannelImage image) {
        image.setProxySender(mSendTransData);
        synchronized (mChannelImageList) {
            if (mCruStatus == ChannelStatus.READY) {
                mChannelImageList.add(image);
            }
        }
        image.onUpdateStatus(mCruStatus);
        ISecurityChannelStatusListener listener = image.getChannelStatusListener();
        if (listener != null) {
            listener.onChannelReady();
        }
    }

    /**
     * 反注册通道
     *
     * @param image 通道镜像
     * @return true 为反注册成功
     */
    public boolean unRegChannelImage(SecurityChannelImage image) {
        synchronized (mChannelImageList) {
            return mChannelImageList.remove(image);
        }
    }

    /**
     * 通知通道正在失效
     */
    protected void notifyChannelInvalid() {
        synchronized (mChannelImageList) {
            for (SecurityChannelImage image : mChannelImageList) {
                ISecurityChannelStatusListener listener = image.getChannelStatusListener();
                if (listener != null) {
                    listener.onChannelInvalid();
                }
                image.onUpdateStatus(mCruStatus);
            }
            mChannelImageList.clear();
        }
    }

    /**
     * 根据requestId分发数据
     *
     * @param requestId 请求id
     * @param realHost  真实目标地址
     * @param port      真实目标端口
     */
    protected void notifyCreateData(String requestId, String realHost, int port) {
        synchronized (mChannelImageList) {
            for (SecurityChannelImage image : mChannelImageList) {
                ISecurityChannelStatusListener listener = image.getChannelStatusListener();
                if (listener instanceof IServerChannelStatusListener) {
                    IServerChannelStatusListener serverListener = (IServerChannelStatusListener) listener;
                    serverListener.onCreateConnect(requestId, realHost, port);
                }
            }
        }
    }

    /**
     * 根据requestId分发数据
     *
     * @param requestId 请求id
     * @param status    状态
     */
    protected void notifyCreateConnectStatus(String requestId, byte status) {
        synchronized (mChannelImageList) {
            for (SecurityChannelImage image : mChannelImageList) {
                ISecurityChannelStatusListener listener = image.getChannelStatusListener();
                if (listener instanceof IClientChannelStatusListener) {
                    IClientChannelStatusListener clientListener = (IClientChannelStatusListener) listener;
                    clientListener.onRemoteCreateConnect(requestId, status);
                }
            }
        }
    }


    /**
     * 根据requestId分发数据
     *
     * @param requestId 请求id
     * @param pctCount  数据包计数（用于udp协议）
     * @param data      数据
     */
    protected void notifyTransData(String requestId, byte pctCount, byte[] data) {
        synchronized (mChannelImageList) {
            for (SecurityChannelImage image : mChannelImageList) {
                ISecurityChannelStatusListener listener = image.getChannelStatusListener();
                if (listener instanceof IClientChannelStatusListener) {
                    IClientChannelStatusListener clientListener = (IClientChannelStatusListener) listener;
                    clientListener.onChannelTransData(requestId, pctCount, data);
                }
            }
        }
    }

    /**
     * 通道建立链接后回调
     *
     * @param channelClient 当前通道客户端
     * @param sender        当前通道客户端的数据发送者
     * @param receiver      当前通道客户端的数据接收者
     */
    protected void onChannelReady(SecurityChannelClient channelClient, SecuritySender sender, SecurityReceiver receiver) {
        synchronized (mReceiveTransData) {
            mCruStatus = ChannelStatus.READY;
        }
        SecurityProtocolParser process = new SecurityProtocolParser();
        process.setResponseSender(sender);
        process.setTransDataListener(mReceiveTransData);
        receiver.setProtocolParser(process);

        this.mSender = sender;
//        this.mReceiver = receiver;

        boolean isServerMode = SecurityChannelContext.getInstance().isServerMode();
        if (!isServerMode) {
            String machineId = SecurityChannelContext.getInstance().getMachineId();
            String md5MachineId = Md5Helper.md5_32(machineId);
            //获取加密的方式
            String encryption = SecurityChannelContext.getInstance().getEncryption();
            //客户端模式下通道建立成功就发init协议数据进行链接操作
            sender.requestInitData(md5MachineId, encryption);
        }
        channelClient.onRegistrarReady();
    }

    /**
     * 通道失效回调
     */
    protected void onChannelInvalid() {
        synchronized (mReceiveTransData) {
            mCruStatus = ChannelStatus.INVALID;
        }
        notifyChannelInvalid();
    }

}
