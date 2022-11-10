package com.jav.net.security.protocol;


import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.channel.joggle.CmdType;

import java.nio.ByteBuffer;

/**
 * 服务器之间同步数据，10分钟请求一次，实现分布式
 *
 * @author yyz
 */
public class SyncProtocol extends ProxyProtocol {

    public SyncProtocol(String machine, byte[] data) {
        super(machine, data);
        setEnType(EnType.NO_ENCODE.getType());
    }

    @Override
    byte cmdType() {
        return CmdType.SYNC.getCmd();
    }

    @Override
    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        if (sendData() == null) {
            return null;
        }
        int length = sendData().length + 45;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putInt(length);
        buffer.putLong(time());
        buffer.put(cmdType());
        buffer.put(machineId());
        buffer.put(sendData());
        return buffer;
    }
}
