package com.jav.net.security.channel;

import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.net.nio.NioClientFactory;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.bean.SecuritySyncEntity;
import com.jav.net.security.channel.joggle.ISyncServerEventCallBack;
import com.jav.net.security.protocol.SyncProtocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 分布式同步服务meter,主要是代理service的服务提供接口调用
 *
 * @author yyz
 */
public class SecuritySyncMeter extends SecurityChanelMeter {


    private Map<String, SecuritySyncEntity> mSyncInfo;

    private NioClientFactory mClientFactory;

    private SecuritySyncEntity mLocalSyncInfo;


    public SecuritySyncMeter(SecurityChannelContext context) {
        super(context);
        mLocalSyncInfo = new SecuritySyncEntity(context.getMachineId(), context.getSyncHost());
        mLocalSyncInfo.setProxyPort(context.getSyncPort());
        ReceiveProxy receiveProxy = new ReceiveProxy();
        ParserCallBackRegistrar registrar = new ParserCallBackRegistrar(receiveProxy);
        setProtocolParserCallBack(registrar);
        mClientFactory = new NioClientFactory();
        mClientFactory.open();
    }


    /**
     * 接收sync事件回调
     */
    private class ReceiveProxy implements ISyncServerEventCallBack {

        @Override
        public void onRespondSyncCallBack(byte status, int proxyPort, String machineId, long loadCount) {
            // 保存远程服务的负载信息
            updateSyncData(machineId, loadCount);
            if (status == SyncProtocol.Status.REQ.getStatus()) {
                int localProxyPort = 0;
                long localServerLoadCount = 0;
                String localMachineId = mContext.getMachineId();
                if (mLocalSyncInfo != null) {
                    localProxyPort = mLocalSyncInfo.getProxyPort();
                    localServerLoadCount = mLocalSyncInfo.getLoadCount();
                }
                // 响应本地服务的负载信息
                SecuritySyncSender syncSender = getSender();
                syncSender.respondSyncData(localMachineId, localProxyPort, localServerLoadCount);
            }
        }
    }

    @Override
    protected void initEncryptionType() {
        mDataSafeManager.init(EncryptionType.BASE64);
    }


    /**
     * 更新本机的负载值
     *
     * @param loadCount
     */
    public void updateLocalServerSyncInfo(long loadCount) {
        if (mLocalSyncInfo != null) {
            mLocalSyncInfo.updateLoadCount(loadCount);
        }
    }

    /**
     * 获取本机负载值
     *
     * @return
     */
    public long getLocalServerLoadCount() {
        if (mLocalSyncInfo != null) {
            return mLocalSyncInfo.getLoadCount();
        }
        return 0;
    }


    /**
     * 加载需要同步的服务列表，key为机器id,value 为服务地址
     *
     * @param syncServer
     */
    public void loadSyncList(Map<String, String> syncServer) {
        if (syncServer == null || syncServer.isEmpty()) {
            return;
        }
        mSyncInfo = new HashMap(syncServer.size());
        Set<Map.Entry<String, String>> entrySet = syncServer.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            String value = entry.getValue();
            String[] arrays = value.split(":");
            if (arrays.length != 2) {
                continue;
            }
            String key = entry.getKey();
            int port = Integer.parseInt(arrays[1]);
            SecuritySyncEntity entity = new SecuritySyncEntity(key, arrays[0]);
            entity.setProxyPort(port);
            mSyncInfo.put(key, entity);
            SecuritySyncServerReception reception = new SecuritySyncServerReception(mContext);
            reception.setAddress(arrays[0], port);
            mClientFactory.getNetTaskComponent().addExecTask(reception);
        }
    }

    public void updateSyncData(String mid, long loadData) {
        if (mSyncInfo == null) {
            return;
        }
        SecuritySyncEntity entity = mSyncInfo.get(mid);
        if (entity != null) {
            entity.updateLoadCount(loadData);
        }
    }

    /**
     * 获取低负载的服务
     *
     * @return
     */
    public String getLowLoadServer() {
        if (mSyncInfo == null) {
            return null;
        }
        Collection<SecuritySyncEntity> collection = mSyncInfo.values();
        SecuritySyncEntity bestTarget = null;
        for (SecuritySyncEntity entity : collection) {
            if (bestTarget == null) {
                if (entity.getProxyPort() > 0) {
                    bestTarget = entity;
                }
            } else {
                if (entity.getProxyPort() > 0 && bestTarget.getLoadCount() > entity.getLoadCount()) {
                    bestTarget = entity;
                }
            }
        }
        return bestTarget == null ? null : bestTarget.getProxyHost() + ":" + bestTarget.getProxyPort();
    }

}
