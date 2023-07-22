package com.jav.net.security.channel;


import com.jav.common.log.LogDog;
import com.jav.common.util.NotRetLockLock;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.nio.NioBalancedClientFactory;
import com.jav.net.nio.NioClientTask;
import com.jav.net.nio.NioServerFactory;
import com.jav.net.security.channel.base.AbsSecurityServer;
import com.jav.net.security.channel.base.ChannelStatus;
import com.jav.net.security.channel.joggle.IClientChannelStatusListener;
import com.jav.net.security.channel.joggle.ISecurityChannelChangeListener;

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
     * 是否已经链接服务
     */
    private volatile boolean mIsConnect = false;

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

    /**
     * 不可重入锁
     */
    private NotRetLockLock mLock;


    private SecurityChannelBoot() {
        mClientChanelList = new ArrayList<>();
        mLock = new NotRetLockLock();
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
    private ISecurityChannelChangeListener mChangeListener = new ISecurityChannelChangeListener() {
        @Override
        public void onRemoteLowLoadServerConnect(String lowLoadHost, int lowLoadPort) {
            //链接低负载的服务
            resetConnectSecurityServer(lowLoadHost, lowLoadPort);
        }
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
            mClientChanelList.remove(channelClient);
            mChannelIndex = 0;
            SecurityChannelClient newChannelClient = createChannel();
            if (newChannelClient == null) {
                return null;
            }
            //如果当前的通道
            ISecurityChannelChangeListener changeListener = chanelMeter.getmChannelChangeListener();
            chanelMeter = newChannelClient.getChanelMeter();
            chanelMeter.setChannelChangeListener(changeListener);
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
        if (mContext.getConnectHost() == null || mContext.getConnectPort() <= 0) {
            return null;
        }
        SecurityChannelClient channelClient = new SecurityChannelClient(mContext);
        channelClient.setAddress(mContext.getConnectHost(), mContext.getConnectPort());
        INetTaskComponent<NioClientTask> container = mClientFactory.getNetTaskComponent();
        container.addExecTask(channelClient);
        mClientChanelList.add(channelClient);
        LogDog.d("## connect security host = " + mContext.getConnectHost() + ":" + mContext.getConnectPort());
        return channelClient;
    }


    /**
     * 连接安全服务端
     */
    public void startConnectSecurityServer() {
        if (mIsConnect) {
            return;
        }
        for (int count = 0; count < mContext.getChannelNumber(); count++) {
            SecurityChannelClient channelClient = createChannel();
            if (count == 0 && channelClient != null) {
                //只给第一个通道配置监听
                SecurityClientChanelMeter clientMeter = channelClient.getChanelMeter();
                clientMeter.setChannelChangeListener(mChangeListener);
            }
        }
        mIsConnect = true;
    }

    /**
     * 初始化客户端链接
     */
    private void initClientFactory() {
        mClientFactory = new NioBalancedClientFactory(WORK_COUNT);
        mClientFactory.open();
    }

    /**
     * 启动安全服务
     */
    public void startupSecurityServer() {
        List<AbsSecurityServer> serverArray = mContext.getSecurityServer();
        if (serverArray != null) {
            // 开启主服务
            mServerFactory = new NioServerFactory();
            mServerFactory.open();
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
        LogDog.w("## resetConnectSecurityServer lowLoadHost:" + host + ":" + port);
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
        NotRetLockLock.NotRetLockKey lockKey = mLock.lock();
        try {
            for (SecurityChannelClient client : mClientChanelList) {
                SecurityClientChanelMeter meter = client.getChanelMeter();
                if (meter.getCruStatus() == ChannelStatus.INVALID) {
                    break;
                }
                SecurityClientChannelImage image = meter.findListenerToImage(listener);
                if (image != null) {
                    mLock.unlock(lockKey);
                    return;
                }
            }
            SecurityClientChanelMeter chanelMeter = pollingChannel();
            if (chanelMeter != null) {
                SecurityClientChannelImage image = SecurityClientChannelImage.builderClientChannelImage(listener);
                chanelMeter.regClientChannelImage(image);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mLock.unlock(lockKey);
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
        NotRetLockLock.NotRetLockKey lockKey = mLock.lock();
        for (SecurityChannelClient client : mClientChanelList) {
            SecurityClientChanelMeter meter = client.getChanelMeter();
            if (meter.unRegClientChannelImage(image)) {
                break;
            }
        }
        mLock.unlock(lockKey);
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
            NotRetLockLock.NotRetLockKey lockKey = mLock.lock();
            mChannelIndex = 0;
            mClientChanelList.clear();
            mLock.unlock(lockKey);
            mIsInit = false;
        }
        mIsConnect = false;
    }
}
