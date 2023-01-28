package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 服务器之间同步数据，10分钟请求一次，实现分布式
 *
 * @author yyz
 */
public class SyncProtocol extends AbsProxyProtocol {

    public enum Status {
        /**
         * 请求状态
         */
        REQ((byte) 1),
        /**
         * 响应状态
         */
        REP((byte) 2);

        private byte mStatus;

        Status(byte status) {
            mStatus = status;
        }


        public byte getStatus() {
            return mStatus;
        }
    }

    /**
     * 不包含length的4个字节的长度
     */
    private static final int HEAD_LENGTH = 49;

    private long mLoadCount;
    private int mPort;
    private byte mStatus;

    public SyncProtocol(String machine, Status status, int port, long loadCount) {
        super(machine);
        setEnType(EnType.NO_ENCODE.getType());
        mLoadCount = loadCount;
        mPort = port;
        mStatus = status.getStatus();
    }

    @Override
    byte cmdType() {
        return CmdType.SYNC.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        int length = sendData().length + HEAD_LENGTH;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putInt(length);
        buffer.putLong(time());
        buffer.put(cmdType());
        buffer.put(machineId());
        buffer.put(mStatus);
        buffer.putInt(mPort);
        buffer.putLong(mLoadCount);
        return buffer;
    }
}
