package com.jav.net.security.channel;

import com.jav.net.security.channel.joggle.IServerSyncStatusListener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 服务模式的通道的镜像
 *
 * @author yyz
 */
public class SecurityServerSyncImage {


    private Map<String, IServerSyncStatusListener> mListener;

    private SecuritySyncSender mSender;

    private SecurityServerSyncImage() {
        mListener = new LinkedHashMap();
    }

    /**
     * 添加sync服务端的通道镜像监听
     *
     * @param listener 监听器
     */
    public void addListener(IServerSyncStatusListener listener) {
        if (listener == null) {
            return;
        }
        mListener.put(listener.getMachineId(), listener);
    }

    /**
     * 移除sync服务端的通道镜像监听
     *
     * @param listener 监听器
     */
    public void removerListener(IServerSyncStatusListener listener) {
        if (listener == null) {
            return;
        }
        mListener.remove(listener.getMachineId());
    }


    protected IServerSyncStatusListener getListener(String mid) {
        if (mid == null) {
            return null;
        }
        return mListener.get(mid);
    }


    /**
     * 同步machine id
     *
     * @param serverMachineId 服务端的machine id
     * @param clientMachineId 客户端的machine id
     */
    public void requestSyncMid(String serverMachineId, String clientMachineId) {
        mSender.requestSyncMid(serverMachineId, clientMachineId);
    }

    /**
     * 响应同步machine id 结果
     *
     * @param serverMachineId 服务端的machine id
     * @param clientMachineId 客户端的machine id
     * @param status          结果
     */
    public void respondSyncMid(String serverMachineId, String clientMachineId, byte status) {
        mSender.respondSyncMid(serverMachineId, clientMachineId, status);
    }


    /**
     * 初始化数据发送器
     *
     * @param sender
     */
    protected void init(SecuritySyncSender sender) {
        mSender = sender;
    }


    private static final class InnerCore {
        private static SecurityServerSyncImage sImage = new SecurityServerSyncImage();
    }


    /**
     * 创建sync服务端的通道镜像
     *
     * @return 通道镜像
     */
    public final static SecurityServerSyncImage getInstance() {
        return InnerCore.sImage;
    }


}
