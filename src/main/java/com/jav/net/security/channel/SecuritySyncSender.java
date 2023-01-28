package com.jav.net.security.channel;

import com.jav.net.entity.MultiByteBuffer;
import com.jav.net.security.protocol.SyncProtocol;

import java.nio.ByteBuffer;

/**
 * sync服务数据发送者
 *
 * @author yyz
 */
public class SecuritySyncSender extends SecuritySender {

    protected void respondSyncData(String machineId, int proxyPort, long loadCount) {
        SyncProtocol syncProtocol = new SyncProtocol(machineId, SyncProtocol.Status.REP, proxyPort, loadCount);
        ByteBuffer encodeData = syncProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
    }

    protected void requestSyncData(String machineId, int proxyPort, long loadCount) {
        SyncProtocol syncProtocol = new SyncProtocol(machineId, SyncProtocol.Status.REQ, proxyPort, loadCount);
        ByteBuffer encodeData = syncProtocol.toData(mEncryptComponent);
        mCoreSender.sendData(new MultiByteBuffer(encodeData));
    }

}
