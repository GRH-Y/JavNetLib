package com.jav.net.security.channel;


import com.jav.common.log.LogDog;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.nio.NioBalancedClientFactory;
import com.jav.net.nio.NioClientTask;
import com.jav.net.nio.NioServerFactory;
import com.jav.net.security.channel.base.AbsSecurityServer;
import com.jav.net.security.channel.joggle.ChannelStatus;
import com.jav.net.security.channel.joggle.IClientChannelStatusListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ChannelManager 通道管理器,管理通道的创建和注册使用通道功能
 *
 * @author yyz
 */
public class SecurityChannelManager {

    private static final int WORK_COUNT = 2;

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
    private NioBalancedClientFactory mClientFactory;

    /**
     * socket服务端通信
     */
    private NioServerFactory mServerFactory;

    /**
     * 指向正在使用的通道索引
     */
    private int mChannelIndex = 0;

    private SecurityChannelManager() {
        mClientChanelList = new ArrayList<>();
    }

    private static final class InnerClass {
        public static final SecurityChannelManager sManager = new SecurityChannelManager();
    }

    public static SecurityChannelManager getInstance() {
        return InnerClass.sManager;
    }


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
        SecurityChannelClient channelClient = mClientChanelList.get(mChannelIndex);
        SecurityClientChanelMeter chanelMeter = channelClient.getChanelMeter();
        if (chanelMeter.getCruStatus() == ChannelStatus.INVALID) {
            mClientChanelList.remove(channelClient);
            channelClient = createChannel();
            chanelMeter = channelClient.getChanelMeter();
        }
        mChannelIndex++;
        if (mChannelIndex == mContext.getChannelNumber()) {
            mChannelIndex = 0;
        }
        return chanelMeter;
    }

    /**
     * 根据参数host和port创建通道链接
     *
     * @return 返回创建的通道
     */
    private SecurityChannelClient createChannel() {
        SecurityChannelClient channelClient = new SecurityChannelClient(mContext);
        channelClient.setAddress(mContext.getConnectHost(), mContext.getConnectPort());
        INetTaskComponent<NioClientTask> container = mClientFactory.getNetTaskComponent();
        container.addExecTask(channelClient);
        mClientChanelList.add(channelClient);
        LogDog.d("connect host = " + mContext.getConnectHost() + ":" + mContext.getConnectPort());
        return channelClient;
    }

    /**
     * 初始化SyncMeter
     */
    private void initSyncMeter() {
        Map<String, String> syncServer = mContext.getSyncServer();
        SecuritySyncMeter syncMeter = new SecuritySyncMeter(mContext);
        syncMeter.loadSyncList(syncServer);
        mContext.setSyncMeter(syncMeter);
    }

    /**
     * 连接代理服务端
     */
    private void startConnectProxyServer() {
        for (int count = 0; count < mContext.getChannelNumber(); count++) {
            createChannel();
        }
    }


    private void initClientFactory() {
        mClientFactory = new NioBalancedClientFactory(WORK_COUNT);
        mClientFactory.open();
    }

    /**
     * 启动服务
     */
    private void startupServer() {
        List<AbsSecurityServer> serverArray = mContext.getSecurityServer();
        if (serverArray != null) {
            // 开启主服务
            mServerFactory = new NioServerFactory();
            mServerFactory.open();
            for (AbsSecurityServer server : serverArray) {
                server.init(mContext);
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
    protected void resetConnectLowLoadServer(String host, int port) {
        mContext.resetConnectSecurityServer(host, port);
        for (SecurityChannelClient client : mClientChanelList) {
            mClientFactory.getNetTaskComponent().addUnExecTask(client);
        }
    }

    /**
     * 重新链接
     *
     * @param client
     */
    protected void resetChannel(SecurityChannelClient client) {
        mClientFactory.getNetTaskComponent().addExecTask(client);
    }


    /**
     * 初始化
     *
     * @param context
     */
    public void init(SecurityChannelContext context) {
        mContext = context;
        if (mIsInit) {
            release();
        }
        initClientFactory();
        if (context.isServerMode()) {
            initSyncMeter();
        } else {
            startConnectProxyServer();
        }
        startupServer();
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
        synchronized (mClientChanelList) {
            for (SecurityChannelClient client : mClientChanelList) {
                SecurityClientChanelMeter meter = client.getChanelMeter();
                SecurityClientChannelImage image = meter.findListenerToImage(listener);
                if (image != null) {
                    return;
                }
            }
        }
        SecurityClientChanelMeter chanelMeter = pollingChannel();
        SecurityClientChannelImage image = SecurityClientChannelImage.builderClientChannelImage(listener);
        chanelMeter.regClientChannelImage(image);
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
        synchronized (mClientChanelList) {
            for (SecurityChannelClient client : mClientChanelList) {
                SecurityClientChanelMeter meter = client.getChanelMeter();
                if (meter.unRegClientChannelImage(image)) {
                    break;
                }
            }
        }
    }

    /**
     * 资源回收，释放通道组
     */
    public void release() {
        if (mIsInit) {
            if (mClientFactory != null) {
                mClientFactory.close();
            }
            if (mServerFactory != null) {
                mServerFactory.close();
            }
            synchronized (mClientChanelList) {
                mClientChanelList.clear();
            }
            mIsInit = false;
        }
    }
}
