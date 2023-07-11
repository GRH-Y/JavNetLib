package com.jav.net.security.channel;


import com.jav.common.cryption.joggle.IDecryptComponent;
import com.jav.common.util.IoEnvoy;
import com.jav.net.nio.NioReceiver;
import com.jav.net.security.channel.base.UnusualBehaviorType;
import com.jav.net.security.channel.joggle.ISecurityReceiver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * 协议格式
 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－|
 * ｜  length（4Byte） ｜    time（8Byte）    ｜    cmd_type（1Byte）  ｜   [m_id or req_id]（32Byte）        ｜
 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
 * ｜                                            data                                                        |
 * ｜－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－｜
 * SecurityReceiver 安全传输协议
 *
 * @author yyz
 */
public class SecurityReceiver implements ISecurityReceiver {


    private enum SRState {
        /**
         * 处理包的长度状态
         */
        LENGTH,
        /**
         * 处理包的数据内容
         */
        DATA,
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
     * 最大的数据长度
     */
    private final static int MAX_LENGTH = 65535;

    /**
     * 真正的数据接收者
     */
    private CoreReceiver mCoreReceiver;
    /**
     * 安全协议的解析器
     */
    private SecurityProtocolParser mProtocolParser;

    /**
     * 解密组件
     */
    private IDecryptComponent mDecryptComponent;


    private class CoreReceiver extends NioReceiver {
        @Override
        protected void onReadNetData(SocketChannel channel) throws Throwable {
            processLength(channel);
            processData(channel);
        }
    }

    public SecurityReceiver() {
        mCoreReceiver = new CoreReceiver();
    }

    @Override
    public void setProtocolParser(SecurityProtocolParser parser) {
        this.mProtocolParser = parser;
    }

    @Override
    public void setDecryptComponent(IDecryptComponent decryptComponent) {
        this.mDecryptComponent = decryptComponent;
    }

    /**
     * 获取真正的数据接收者
     *
     * @return
     */
    public NioReceiver getCoreReceiver() {
        return mCoreReceiver;
    }

    public void reset() {
        mState = SRState.LENGTH;
        mFullData = null;
    }

    /**
     * 检查返回code
     *
     * @param retCode code
     * @throws IOException
     */
    private void checkReturnCode(int retCode) throws IOException {
        if (retCode == IoEnvoy.FAIL) {
            throw new IOException("channel is close !!!");
        }
    }

    /**
     * 解密数据
     *
     * @param encodeData 加密的数据
     * @return 解密后的数据
     */
    private ByteBuffer decodeData(ByteBuffer encodeData) {
        byte[] data = encodeData.array();
        byte[] decodeData = mDecryptComponent.onDecrypt(data);
        return ByteBuffer.wrap(decodeData);
    }

    /**
     * 解析数据的长度
     *
     * @param channel
     * @throws IOException
     */
    private void processLength(SocketChannel channel) throws IOException {
        if (mState == SRState.LENGTH) {
            int retCode = IoEnvoy.readToFull(channel, mLength);
            if (retCode == IoEnvoy.SUCCESS) {
                mLength.flip();
                int length = mLength.getInt();
                if (length <= 0 || length > MAX_LENGTH) {
                    String remoteHost = getRemoteHost(channel);
                    mProtocolParser.callBackDeny(remoteHost, UnusualBehaviorType.EXP_LENGTH);
                }
                mFullData = ByteBuffer.allocate(length);
                mLength.clear();
                mState = SRState.DATA;
            } else {
                checkReturnCode(retCode);
            }
        }
    }

    /**
     * 解析数据主体
     *
     * @param channel
     * @throws IOException
     */
    private void processData(SocketChannel channel) throws IOException {
        if (mState == SRState.DATA) {
            int retCode = IoEnvoy.readToFull(channel, mFullData);
            if (retCode == IoEnvoy.SUCCESS) {
                if (mProtocolParser != null) {
                    mFullData.flip();
                    ByteBuffer decodeData = decodeData(mFullData);
                    String remoteHost = getRemoteHost(channel);
                    mProtocolParser.parserReceiverData(remoteHost, decodeData);
                }
                mFullData = null;
                mState = SRState.LENGTH;
            } else {
                checkReturnCode(retCode);
            }
        }
    }

    /**
     * 获取当前链接的远程目标地址
     *
     * @param channel 远程通道
     * @return 远程目标地址
     */
    private String getRemoteHost(SocketChannel channel) {
        String remoteHost = null;
        try {
            InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
            remoteHost = address.getHostString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return remoteHost;
    }

}
