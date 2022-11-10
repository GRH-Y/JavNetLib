package com.jav.net.security.channel;


import com.jav.common.log.LogDog;
import com.jav.common.util.IoEnvoy;
import com.jav.net.nio.NioReceiver;
import com.jav.net.security.channel.joggle.CmdType;
import com.jav.net.security.channel.joggle.ISecurityProtocolParserProcess;
import com.jav.net.security.channel.joggle.ISecurityReceiver;
import com.jav.net.security.protocol.ProxyProtocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 协议格式
 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－|
 * ｜  length（4Byte） ｜    time（8Byte）    ｜    cmd_type（1Byte）  ｜   [m_id or req_id]（32Byte）  ｜
 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
 * ｜                                       data                                                     |
 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
 * SecurityReceiver 安全传输协议
 *
 * @author yyz
 */
public class SecurityReceiver extends NioReceiver implements ISecurityReceiver {


    private enum SRState {
        LENGTH, DATA, // NONE
    }

    private final ByteBuffer mLength = ByteBuffer.allocate(4);

    /**
     * 完整的数据
     */
    private ByteBuffer mFullData = null;

    /**
     * 解析数据的流程状态
     */
    private SRState mState = SRState.LENGTH;

    /**
     * 安全协议的解析器
     */
    private SecurityProtocolParser mProtocolParser;

    public void setProtocolParser(SecurityProtocolParser parser) {
        this.mProtocolParser = parser;
    }

    @Override
    public SecurityProtocolParser getProtocolParser() {
        return mProtocolParser;
    }

    @Override
    protected void onReadNetData(SocketChannel channel) throws Throwable {
        processInit(channel);
        processData(channel);
    }

    private void processInit(SocketChannel channel) throws IOException {
        if (mState == SRState.LENGTH) {
            int ret = IoEnvoy.readToFull(channel, mLength);
            if (ret == IoEnvoy.SUCCESS) {
                mLength.flip();
                int length = mLength.getInt();
                if (length <= 0) {
                    if (mProtocolParser != null) {
                        ISecurityProtocolParserProcess process = mProtocolParser.getParserProcess();
                        process.onDeny("非法数据长度!!!");
                        throw new RuntimeException("Illegal data length !!!");
                    }
                    return;
                }
                mFullData = ByteBuffer.allocate(length);
                mState = SRState.DATA;
            } else if (ret == IoEnvoy.FAIL) {
                if (mProtocolParser != null) {
                    ISecurityProtocolParserProcess process = mProtocolParser.getParserProcess();
                    process.onError();
                }
                throw new IOException("channel is close !!!");
            }
        }
    }

    private void processData(SocketChannel channel) throws IOException {
        if (mState == SRState.DATA) {
            int ret = IoEnvoy.readToFull(channel, mFullData);
            if (ret == IoEnvoy.SUCCESS) {
                if (mProtocolParser != null) {
                    ISecurityProtocolParserProcess process = mProtocolParser.getParserProcess();
                    mFullData.flip();
                    ByteBuffer decodeData = process.decodeData(mFullData);
                    //解析校验时间字段
                    parseTime(decodeData);
                    //解析cmd字段
                    byte cmd = decodeData.get();
                    if (cmd == CmdType.INIT.getCmd()) {
                        //解析校验machine id字段
                        parseMachineId(decodeData);
                    } else if (cmd == CmdType.REQUEST.getCmd() || cmd == CmdType.TRANS.getCmd()) {
                        parseChannelId(decodeData);
                    }
                    process.onExecCmd(cmd, decodeData);
                }
                mFullData = null;
                mState = SRState.LENGTH;
            } else if (ret == IoEnvoy.FAIL) {
                if (mProtocolParser != null) {
                    ISecurityProtocolParserProcess process = mProtocolParser.getParserProcess();
                    process.onError();
                }
                throw new IOException("channel is close !!!");
            }
        }
    }

    /**
     * 解析校验时间字段
     *
     * @param decodeData 要校验的数据
     */
    private void parseTime(ByteBuffer decodeData) {
        long time = decodeData.getLong();
        ISecurityProtocolParserProcess process = mProtocolParser.getParserProcess();
        boolean isDeny = process.onCheckTime(time);
        if (!isDeny) {
            process.onDeny("时间过期或者非法时间!!!");
            throw new RuntimeException("Time expired or illegal time !!!");
        }
        LogDog.d("时间有效性校验通过!");
    }

    /**
     * 解析校验machine id字段
     *
     * @param decodeData 要校验的数据
     */
    private void parseMachineId(ByteBuffer decodeData) {
        byte[] mid = new byte[ProxyProtocol.MACHINE_LENGTH];
        decodeData.get(mid);
        ISecurityProtocolParserProcess process = mProtocolParser.getParserProcess();
        boolean isDeny = process.onCheckMachineId(new String(mid));
        if (!isDeny) {
            process.onDeny("非法机器号!!!");
            throw new RuntimeException("Illegal machine number !!!");
        }
        LogDog.d("机器id校验通过!");
    }

    private void parseChannelId(ByteBuffer decodeData) {
        byte[] channelIdByte = new byte[ProxyProtocol.CHANNEL_LENGTH];
        decodeData.get(channelIdByte);
        String channelId = new String(channelIdByte);
        ISecurityProtocolParserProcess process = mProtocolParser.getParserProcess();
        boolean isDeny = process.onCheckChannelId(channelId);
        if (!isDeny) {
            process.onDeny("非法channel id!!!");
            throw new RuntimeException("Channel id illegal !!!");
        }
        LogDog.d("channel id校验通过!");
    }

}
