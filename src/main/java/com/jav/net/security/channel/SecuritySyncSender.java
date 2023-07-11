package com.jav.net.security.channel;

import com.jav.net.entity.MultiByteBuffer;
import com.jav.net.security.protocol.SyncProtocol;
import com.jav.net.security.protocol.base.SyncOperateCode;

import java.nio.ByteBuffer;

/**
 * sync服务数据发送者
 *
 * @author yyz
 */
public class SecuritySyncSender extends SecuritySender {


    /**
     * 响应sync avg 请求，
     *
     * @param machineId 本服务的machine id （注意不是请求端的machine id）
     * @param proxyPort 当前中转服务的端口号
     * @param loadAvg   当前服务的负载值
     */
    protected void respondSyncAvg(String machineId, int proxyPort, byte loadAvg) {
        SyncProtocol syncProtocol = new SyncProtocol(machineId, proxyPort, loadAvg);
        syncProtocol.setOperateCode(SyncOperateCode.SYNC_AVG.getCode());
        ByteBuffer encodeData = syncProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
    }

    /**
     * 请求 sync avg
     *
     * @param machineId 本服务的machine id
     * @param proxyPort 当前中转服务的端口号
     * @param loadAvg   当前服务的负载值
     */
    protected void requestSyncAvg(String machineId, int proxyPort, byte loadAvg) {
        SyncProtocol syncProtocol = new SyncProtocol(machineId, proxyPort, loadAvg);
        syncProtocol.setOperateCode(SyncOperateCode.SYNC_AVG.getCode());
        ByteBuffer encodeData = syncProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
    }


    /**
     * 响应 sync data 请求
     *
     * @param serverMachineId 本服务的machine id （注意不是请求端的machine id）
     * @param clientMachineId 客户端的machine id
     * @param status
     */
    protected void respondSyncMid(String serverMachineId, String clientMachineId, byte status) {
        SyncProtocol syncProtocol = new SyncProtocol(serverMachineId);
        byte operateCode = (byte) (status | SyncOperateCode.SYNC_MID.getCode());
        syncProtocol.setOperateCode(operateCode);
        syncProtocol.setSendData(clientMachineId.getBytes());
        ByteBuffer encodeData = syncProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
    }

    /**
     * 请求 sync data
     *
     * @param serverMachineId 本服务的machine id
     * @param clientMachineId 客户端的machine id
     */
    protected void requestSyncMid(String serverMachineId, String clientMachineId) {
        SyncProtocol syncProtocol = new SyncProtocol(serverMachineId);
        syncProtocol.setOperateCode(SyncOperateCode.SYNC_MID.getCode());
        syncProtocol.setSendData(clientMachineId.getBytes());
        ByteBuffer encodeData = syncProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
    }

}
