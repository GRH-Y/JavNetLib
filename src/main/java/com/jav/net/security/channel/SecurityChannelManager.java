package com.jav.net.security.channel;


import com.jav.common.log.LogDog;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.nio.NioClientFactory;
import com.jav.net.nio.NioClientTask;
import com.jav.net.security.channel.joggle.ChannelStatus;
import com.jav.net.security.channel.joggle.IClientChannelStatusListener;

import java.util.ArrayList;
import java.util.List;

/**
 * ChannelManager 通道管理器,管理通道的创建和注册使用通道功能
 *
 * @author yyz
 */
public class SecurityChannelManager {

    private final NioClientFactory mClientFactory;

    /**
     * 最大的通道数
     */
    private static final int MAX_CHANNEL = 1;

    private String mHost;

    private int mPort;

    private boolean mIsInit = false;

    /**
     * 通道集合
     */
    private final List<SecurityChannelClient> mSecurityChanel;

    /**
     * 指向正在使用的通道索引
     */
    private int mChannelIndex = 0;

    private SecurityChannelManager() {
        mSecurityChanel = new ArrayList<>();
        mClientFactory = new NioClientFactory();
    }

    private static final class InnerClass {
        public static final SecurityChannelManager sManager = new SecurityChannelManager();
    }

    public static SecurityChannelManager getInstance() {
        return InnerClass.sManager;
    }


    public boolean isInit() {
        return mIsInit;
    }

    /**
     * 轮询选择通道
     *
     * @return 返回可用的通道
     */
    private SecurityChanelMeter pollingChannel() {
        if (mChannelIndex == MAX_CHANNEL) {
            mChannelIndex = 0;
        }
        SecurityChannelClient channelClient = mSecurityChanel.get(mChannelIndex);
        SecurityChanelMeter chanelMeter = channelClient.getChanelMeter();
        if (chanelMeter.getCruStatus() == ChannelStatus.READY) {
            mSecurityChanel.remove(channelClient);
            channelClient = createChannel();
            chanelMeter = channelClient.getChanelMeter();
        }
        return chanelMeter;
    }

    /**
     * 根据参数host和port创建通道链接
     *
     * @return 返回创建的通道
     */
    private SecurityChannelClient createChannel() {
        SecurityChannelClient channelClient = new SecurityChannelClient();
        channelClient.setAddress(mHost, mPort);
        INetTaskComponent<NioClientTask> container = mClientFactory.getNetTaskComponent();
        container.addExecTask(channelClient);
        mSecurityChanel.add(channelClient);
        return channelClient;
    }


    /**
     * 初始化通道组
     *
     * @param host 链接的目标地址
     * @param port 链接目标的端口
     */
    public void init(String host, int port) {
        synchronized (mSecurityChanel) {
            if (mSecurityChanel.isEmpty()) {
                this.mHost = host;
                this.mPort = port;
                LogDog.d("connect host = " + host + ":" + port);
                mClientFactory.open();
                synchronized (mSecurityChanel) {
                    for (int count = 0; count < MAX_CHANNEL; count++) {
                        createChannel();
                    }
                }
                mIsInit = true;
            }
        }
    }

    /**
     * 根据requestId注册通道，返回ChannelPorter 对象用于数据的发生和接收的中转
     *
     * @return 返回注册成功的通道镜像，失败返回null
     */
    public SecurityChannelImage registerChannel(IClientChannelStatusListener listener) {
        synchronized (mSecurityChanel) {
            for (SecurityChannelClient client : mSecurityChanel) {
                SecurityChanelMeter meter = client.getChanelMeter();
                SecurityChannelImage image = meter.findListenerToImage(listener);
                if (image != null) {
                    return image;
                }
            }
            SecurityChanelMeter chanelMeter = pollingChannel();
            SecurityChannelImage image = SecurityChannelImage.builderClientChannelImage(listener);
            chanelMeter.regChannelImage(image);
            return image;
        }
    }

    /**
     * 反注册通道,不再使用该通道来通信
     *
     * @param image 通道镜像
     */
    public void unRegisterChannel(SecurityChannelImage image) {
        if (image == null) {
            return;
        }
        synchronized (mSecurityChanel) {
            for (SecurityChannelClient client : mSecurityChanel) {
                SecurityChanelMeter meter = client.getChanelMeter();
                if (meter.unRegChannelImage(image)) {
                    break;
                }
            }
        }
    }

    /**
     * 资源回收，释放通道组
     */
    public void release() {
        synchronized (mSecurityChanel) {
            mClientFactory.close();
            mSecurityChanel.clear();
        }
    }
}
