package com.jav.net.security.channel;


import com.jav.common.log.LogDog;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.nio.NioClientFactory;
import com.jav.net.nio.NioClientTask;
import com.jav.net.nio.NioServerFactory;
import com.jav.net.security.channel.base.AbsSecurityServer;
import com.jav.net.security.channel.base.ChannelStatus;
import com.jav.net.security.channel.joggle.IClientChannelStatusListener;
import com.jav.net.security.channel.joggle.ISecurityChannelChangeListener;
import com.jav.net.security.guard.ChannelKeepAliveSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * ChannelManager 通道管理器,管理通道的创建和注册使用通道功能
 *
 * @author yyz
 */
public class SecurityChannelBoot {

    /**
     * 工作线程数
     */
    private static final int WORK_COUNT = 4;

    /**
     * 配置信息
     */
    private SecurityChannelContext mContext;

    /**
     * 是否已经初始化
     */
    private volatile boolean mIsInit = false;


    /**
     * 客户端通道集合
     */
    private final List<SecurityChannelClient> mClientChanelList;

    /**
     * socket客户端通信
     */
    private INetFactory mClientFactory;

    /**
     * socket服务端通信
     */
    private NioServerFactory mServerFactory;

    /**
     * 指向正在使用的通道索引
     */
    private int mChannelIndex = 0;

    /**
     * 不可重入锁
     */
    private final Object mLock = new Object();


    private SecurityChannelBoot() {
        mClientChanelList = new ArrayList<>();
    }

    private static final class InnerClass {
        public static final SecurityChannelBoot sManager = new SecurityChannelBoot();
    }

    public static SecurityChannelBoot getInstance() {
        return InnerClass.sManager;
    }

    /**
     * 切换安全服务
     */
    private final ISecurityChannelChangeListener mChangeListener = (lowLoadHost, lowLoadPort) -> {
        //链接低负载的服务
        resetConnectSecurityServer(lowLoadHost, lowLoadPort);
    };


    /**
     * 是否初始化
     *
     * @return true为已经初始化
     */
    public boolean isInit() {
        return mIsInit;
    }

    /**
     * 轮询选择通道
     *
     * @return 返回可用的通道
     */
    private SecurityClientChanelMeter pollingChannel() {
        if (mClientChanelList.isEmpty()) {
            return null;
        }
        SecurityChannelClient channelClient = mClientChanelList.get(mChannelIndex);
        SecurityClientChanelMeter chanelMeter = channelClient.getChanelMeter();
        if (chanelMeter.getCruStatus() == ChannelStatus.INVALID) {
            LogDog.w("#SC# channel is INVALID , need new channel !!!");
            mClientChanelList.remove(channelClient);
            SecurityChannelClient newChannelClient = createChannel();
            if (newChannelClient == null) {
                return null;
            }
            //如果当前的主通道，配置主通道的监听
            ISecurityChannelChangeListener changeListener = chanelMeter.getChannelChangeListener();
            chanelMeter.setChannelChangeListener(null);
            chanelMeter = newChannelClient.getChanelMeter();
            chanelMeter.setChannelChangeListener(changeListener);

            INetTaskComponent<NioClientTask> container = mClientFactory.getNetTaskComponent();
            container.addExecTask(newChannelClient);
        }
        mChannelIndex++;
        if (mChannelIndex == mContext.getChannelNumber()) {
            mChannelIndex = 0;
        }
        LogDog.d("#SC# choose channel index = " + mChannelIndex);
        return chanelMeter;
    }

    /**
     * 根据参数host和port创建通道链接
     *
     * @return 返回创建的通道
     */
    private SecurityChannelClient createChannel() {
        if (mContext.getConnectHost() == null || mContext.getConnectPort() <= 0) {
            return null;
        }
        SecurityChannelClient channelClient = new SecurityChannelClient(mContext);
        channelClient.setAddress(mContext.getConnectHost(), mContext.getConnectPort());
        mClientChanelList.add(channelClient);
        LogDog.d("#SC# connect security host = " + mContext.getConnectHost() + ":" + mContext.getConnectPort());
        return channelClient;
    }


    /**
     * 连接安全服务端
     */
    public void startConnectSecurityServer() {
        if (mClientFactory != null) {
            return;
        }
        mClientFactory = new NioClientFactory(WORK_COUNT);
        mClientFactory.open();

        String machineId = mContext.getMachineId();
        //启动通道保活机制
        ChannelKeepAliveSystem.getInstance().init(machineId);

        for (int count = 0; count < mContext.getChannelNumber(); count++) {
            SecurityChannelClient channelClient = createChannel();
            if (channelClient == null) {
                continue;
            }
            if (count == 0) {
                //只给第一个通道配置监听
                SecurityClientChanelMeter clientMeter = channelClient.getChanelMeter();
                clientMeter.setChannelChangeListener(mChangeListener);
            }
            INetTaskComponent<NioClientTask> container = mClientFactory.getNetTaskComponent();
            container.addExecTask(channelClient);
        }
    }


    /**
     * 启动安全服务
     */
    public void startupSecurityServer() {
        if (mServerFactory != null) {
            return;
        }
        mServerFactory = new NioServerFactory();
        mServerFactory.open();
        List<AbsSecurityServer> serverArray = mContext.getSecurityServer();
        if (serverArray != null) {
            // 开启主服务
            for (AbsSecurityServer server : serverArray) {
                server.init(mContext.isEnableIpBlack());
                mServerFactory.getNetTaskComponent().addExecTask(server);
            }
        }
    }


    /**
     * 断开当前连接,重新链接低负载的服务,channel断开链接后会调用resetChannel()重新连接
     *
     * @param host
     * @param port
     */
    private void resetConnectSecurityServer(String host, int port) {
        mContext.resetConnectSecurityServer(host, port);
        for (SecurityChannelClient client : mClientChanelList) {
            mClientFactory.getNetTaskComponent().addUnExecTask(client);
        }
        LogDog.w("#SC# resetConnectSecurityServer lowLoadHost:" + host + ":" + port);
    }


    /**
     * 初始化
     *
     * @param context
     */
    public void init(SecurityChannelContext context) {
        mContext = context;
        mIsInit = true;
    }


    /**
     * 注册客户端通道
     *
     * @param listener
     * @return 返回注册成功的通道镜像，失败返回null
     */
    public void registerClientChannel(IClientChannelStatusListener listener) {
        if (listener == null || !mIsInit || mContext.isServerMode()) {
            return;
        }
        synchronized (mLock) {
            try {
                SecurityClientChanelMeter chanelMeter = pollingChannel();
                if (chanelMeter != null) {
                    SecurityClientChannelImage image = SecurityClientChannelImage.builderClientChannelImage(listener);
                    chanelMeter.regClientChannelImage(image);
                    LogDog.w("#SC# register Client Channel !!!" + image);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 反注册通道,不再使用该通道来通信
     *
     * @param image 通道镜像
     */
    public void unRegisterClientChannel(SecurityClientChannelImage image) {
        if (image == null || !mIsInit) {
            return;
        }
        synchronized (mLock) {
            for (SecurityChannelClient client : mClientChanelList) {
                SecurityClientChanelMeter meter = client.getChanelMeter();
                if (meter.unRegClientChannelImage(image)) {
                    LogDog.w("#SC# unRegister Client Channel !!!" + image);
                    break;
                }
            }
        }
    }


    /**
     * 资源回收，释放通道组
     */
    public void release() {
        if (mClientFactory != null) {
            mClientFactory.close();
            mClientFactory = null;
        }
        if (mServerFactory != null) {
            mServerFactory.close();
            mServerFactory = null;
        }
        synchronized (mLock) {
            mChannelIndex = 0;
            mClientChanelList.clear();
        }
        ChannelKeepAliveSystem.getInstance().destroy();
        mIsInit = false;
    }
}
