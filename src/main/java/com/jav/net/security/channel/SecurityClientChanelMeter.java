package com.jav.net.security.channel;


import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.net.security.channel.base.ChannelStatus;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.base.UnusualBehaviorType;
import com.jav.net.security.channel.joggle.*;

import java.util.*;

/**
 * ChanelMeter 通道辅助，向外提供服务
 *
 * @author yyz
 */
public class SecurityClientChanelMeter extends SecurityChanelMeter {


    /**
     * 通道镜像集合（client mode）
     */
    private final Map<String, SecurityClientChannelImage> mChannelImageMap;


    /**
     * 延迟通知监听器集合（client mode）
     */
    private final List<RegEntity> mDelayListener;

    /**
     * 切换服务监听
     */
    private ISecurityChannelChangeListener mChannelChangeListener;


    public SecurityClientChanelMeter(SecurityChannelContext context) {
        super(context);
        ReceiveProxy receiveProxy = new ReceiveProxy();
        ParserCallBackRegistrar registrar = new ParserCallBackRegistrar(receiveProxy);
        setProtocolParserCallBack(registrar);
        mChannelImageMap = new HashMap<>();
        mDelayListener = new ArrayList<>();
    }

    /**
     * 设置切换服务监听
     *
     * @param listener
     */
    public void setChannelChangeListener(ISecurityChannelChangeListener listener) {
        this.mChannelChangeListener = listener;
    }

    /**
     * 获取监听器
     *
     * @return
     */
    protected ISecurityChannelChangeListener getmChannelChangeListener() {
        return mChannelChangeListener;
    }

    /**
     * 注册实体
     */
    private class RegEntity {

        private ISecurityChannelStatusListener mListener;
        private SecurityClientChannelImage mImage;

        public RegEntity(ISecurityChannelStatusListener listener, SecurityClientChannelImage image) {
            mListener = listener;
            mImage = image;
        }

        public ISecurityChannelStatusListener getListener() {
            return mListener;
        }

        public SecurityClientChannelImage getImage() {
            return mImage;
        }
    }


    /**
     * 接收分发数据
     */
    private class ReceiveProxy implements IClientEventCallBack {

        @Override
        public void onRespondServerHighLoadCallBack(String lowLoadHost, int lowLoadPort) {
            notifyChannelReset(lowLoadHost, lowLoadPort);
        }

        @Override
        public void onRespondChannelIdCallBack(String channelId) {
            SecurityProxySender proxySender = getSender();
            proxySender.setChannelId(channelId);
            updateCurStatus(ChannelStatus.READY);
            // 链接服务成功后再通知接口
            notifyChannelReady();
        }

        @Override
        public void onRespondRequestStatusCallBack(String requestId, byte status) {
            // 分发请求创建目标链接结果回调
            notifyCreateConnectStatus(requestId, status);
        }

        @Override
        public void onTransData(String requestId, byte[] data) {
            // 分发中转数据
            notifyTransData(requestId, data);
        }

        @Override
        public void onChannelError(UnusualBehaviorType error, Map<String, String> extData) {
            notifyChannelError(error, extData);
        }

    }


    /**
     * 根据通道状态监听器查找通道镜像
     *
     * @param listener 监听器
     * @return 通道镜像
     */
    protected SecurityClientChannelImage findListenerToImage(ISecurityChannelStatusListener listener) {
        synchronized (mChannelImageMap) {
            Collection<SecurityClientChannelImage> collection = mChannelImageMap.values();
            for (SecurityClientChannelImage image : collection) {
                if (image.getChannelStatusListener() == listener) {
                    return image;
                }
            }
        }
        return null;
    }


    /**
     * 注册客户端通道镜像，获得通道的状态，发送数据，接收数据的能力
     *
     * @param image 通道镜像
     */
    public void regClientChannelImage(SecurityClientChannelImage image) {
        if (getCruStatus() == ChannelStatus.INVALID || mContext.isServerMode()) {
            return;
        }
        SecurityProxySender proxySender = getSender();
        image.setProxySender(proxySender);
        if (getCruStatus() == ChannelStatus.NONE) {
            ISecurityChannelStatusListener listener = image.getChannelStatusListener();
            RegEntity regEntity = new RegEntity(listener, image);
            synchronized (mDelayListener) {
                mDelayListener.add(regEntity);
            }
        } else if (getCruStatus() == ChannelStatus.READY) {
            image.updateStatus(getCruStatus());
            ISecurityChannelStatusListener listener = image.getChannelStatusListener();
            listener.onChannelReady(image);
            synchronized (mChannelImageMap) {
                mChannelImageMap.put(image.getRequestId(), image);
            }
        }
    }

    /**
     * 反注册通道
     *
     * @param image 通道镜像
     * @return true 为反注册成功
     */
    public boolean unRegClientChannelImage(SecurityClientChannelImage image) {
        synchronized (mChannelImageMap) {
            return mChannelImageMap.remove(image.getRequestId()) == image;
        }
    }

    /**
     * 清楚所有通道镜像
     */
    protected void clearAllChannelImage() {
        synchronized (mChannelImageMap) {
            mChannelImageMap.clear();
        }
    }

    /**
     * 通知通道可用
     */
    protected void notifyChannelReady() {
        synchronized (mDelayListener) {
            for (RegEntity entity : mDelayListener) {
                SecurityClientChannelImage image = entity.getImage();
                image.updateStatus(getCruStatus());
                entity.getListener().onChannelReady(image);
                synchronized (mChannelImageMap) {
                    mChannelImageMap.put(image.getRequestId(), image);
                }
            }
            mDelayListener.clear();
        }
    }

    /**
     * 通知通道正在失效
     */
    protected void notifyChannelInvalid() {
        Object[] images;
        synchronized (mChannelImageMap) {
            Collection<SecurityClientChannelImage> collection = mChannelImageMap.values();
            images = collection.toArray();
            mChannelImageMap.clear();
        }
        for (Object obj : images) {
            SecurityClientChannelImage image = (SecurityClientChannelImage) obj;
            image.updateStatus(getCruStatus());
            ISecurityChannelStatusListener listener = image.getChannelStatusListener();
            if (listener != null) {
                listener.onChannelInvalid();
            }
        }
    }


    /**
     * 通知通道正在失效
     */
    protected void notifyChannelReset(String lowLoadHost, int lowLoadPort) {
        if (mChannelChangeListener != null) {
            mChannelChangeListener.onRemoteLowLoadServerConnect(lowLoadHost, lowLoadPort);
        }
    }


    /**
     * 根据requestId分发数据
     *
     * @param requestId 请求id
     * @param status    状态
     */
    protected void notifyCreateConnectStatus(String requestId, byte status) {
        SecurityClientChannelImage image;
        synchronized (mChannelImageMap) {
            image = mChannelImageMap.get(requestId);
        }
        if (image != null) {
            ISecurityChannelStatusListener listener = image.getChannelStatusListener();
            if (listener instanceof IClientChannelStatusListener) {
                IClientChannelStatusListener clientListener = (IClientChannelStatusListener) listener;
                clientListener.onRemoteCreateConnect(status);
            }
        }
    }


    /**
     * 根据requestId分发数据
     *
     * @param requestId 请求id
     * @param data      数据
     */
    protected void notifyTransData(String requestId, byte[] data) {
        SecurityClientChannelImage image;
        synchronized (mChannelImageMap) {
            image = mChannelImageMap.get(requestId);
        }
        if (image != null) {
            IClientChannelStatusListener clientListener = image.getChannelStatusListener();
            clientListener.onRemoteTransData(data);
        }
    }

    /**
     * 根据requestId分发数据
     *
     * @param error   错误信息
     * @param extData 错误扩展信息
     */
    protected void notifyChannelError(UnusualBehaviorType error, Map<String, String> extData) {
        Object[] images;
        synchronized (mChannelImageMap) {
            Collection<SecurityClientChannelImage> collection = mChannelImageMap.values();
            images = collection.toArray();
        }
        for (Object obj : images) {
            SecurityClientChannelImage image = (SecurityClientChannelImage) obj;
            ISecurityChannelStatusListener listener = image.getChannelStatusListener();
            if (listener != null) {
                listener.onChannelError(error, extData);
            }
        }
    }

    @Override
    protected void onExtChannelReady() {
        String machineId = mContext.getMachineId();
        // 获取加密的方式
        ChannelEncryption encryption = mContext.getChannelEncryption();
        byte[] initData = null;
        // 根据不同加密方式发送不同的数据
        ChannelEncryption.TransmitEncryption transmitEncryption = encryption.getTransmitEncryption();
        if (transmitEncryption.getEncryptionType() == EncryptionType.AES) {
            String desPassword = transmitEncryption.getPassword();
            initData = desPassword.getBytes();
        }
        // 客户端模式下请求init交互验证,完成init交互验证即可正常转发数据
        SecurityProxySender proxySender = getSender();
        proxySender.requestInitData(machineId, initData, encryption, channelEncryption -> {
            // init交互数据发送完成开始切换加密方式
            ChannelEncryption.TransmitEncryption changeEncryption = channelEncryption.getTransmitEncryption();
            EncryptionType encryptionType = changeEncryption.getEncryptionType();
            configEncryptionMode(encryptionType, channelEncryption);
        });
    }


    /**
     * 通道失效回调
     */
    protected void onChannelInvalid() {
        super.onChannelInvalid();
        notifyChannelInvalid();
    }

}
