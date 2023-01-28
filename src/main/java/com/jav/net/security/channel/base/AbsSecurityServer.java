package com.jav.net.security.channel.base;

import com.jav.net.nio.NioServerTask;
import com.jav.net.security.channel.SecurityChannelContext;
import com.jav.net.security.guard.IpBlackListManager;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * 带有ip黑名单校验的服务端
 *
 * @author yyz
 */
public abstract class AbsSecurityServer extends NioServerTask {

    /**
     * 是否开启黑名单拦截
     */
    protected boolean mIsEnableIPBlack;

    /**
     * 配置信息
     */
    protected SecurityChannelContext mContext;


    public void init(SecurityChannelContext context) {
        this.mContext = context;
        mIsEnableIPBlack = context.isEnableIpBlack();
    }


    @Override
    protected void onAcceptServerChannel(SocketChannel channel) {
        if (mIsEnableIPBlack) {
            boolean isBlack = false;
            try {
                InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
                // 判断ip是否在黑名单
                isBlack = IpBlackListManager.getInstance().isBlackList(address.getHostString());
                if (isBlack) {
                    // 如果是黑名单则直接断开链接
                    channel.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (isBlack) {
                    return;
                }
            }
        }
        onPassChannel(mContext, channel);
    }


    /**
     * 经过黑名单校验后放行回调
     *
     * @param context
     * @param channel
     * @return 返回响应通道的客户端
     */
    protected abstract void onPassChannel(SecurityChannelContext context, SocketChannel channel);

}
