package com.jav.net.security.channel;


import com.jav.common.cryption.DataSafeManager;
import com.jav.common.cryption.RSADataEnvoy;
import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.cryption.joggle.IDecryptComponent;
import com.jav.common.log.LogDog;
import com.jav.common.security.Md5Helper;
import com.jav.common.util.StringEnvoy;
import com.jav.net.security.channel.joggle.CmdType;
import com.jav.net.security.channel.joggle.IReceiverTransDataProxy;
import com.jav.net.security.channel.joggle.ISecurityProtocolParserProcess;
import com.jav.net.security.protocol.ProxyProtocol;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

/**
 * 安全协议解析器
 */
public class SecurityProtocolParser {

    /**
     * init返回数据类型
     */
    private enum InitResult {

        NORMAL((byte) 1), SERVER_IP((byte) 2);

        private final byte mCode;

        InitResult(byte code) {
            mCode = code;
        }

        public byte getCode() {
            return mCode;
        }

        public static InitResult getInstance(byte code) {
            if (NORMAL.getCode() == code) {
                return NORMAL;
            } else if (SERVER_IP.getCode() == code) {
                return SERVER_IP;
            }
            return null;
        }
    }

    private final DataSafeManager mDataSafeManager;

    private IReceiverTransDataProxy mTransDataListener;

    private SecuritySender mResponseSender;

    private final ISecurityProtocolParserProcess mParserProcess;

    private String mChannelId;

    private String mMachineId;

    public SecurityProtocolParser() {
        mDataSafeManager = new DataSafeManager();
        mDataSafeManager.init(EncryptionType.RSA);
        IDecryptComponent decryptComponent = mDataSafeManager.getDecrypt();
        RSADataEnvoy rsaDataEnvoy = decryptComponent.getComponent();

        String publicFile = SecurityChannelContext.getInstance().getPublicFile();
        String privateFile = SecurityChannelContext.getInstance().getPrivateFile();
        try {
            rsaDataEnvoy.init(publicFile, privateFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        mParserProcess = new SecurityProtocolParserProcess();
    }

    /**
     * 获取安全协议解析处理流程
     *
     * @return 返回解析处理者
     */
    protected ISecurityProtocolParserProcess getParserProcess() {
        return mParserProcess;
    }

    /**
     * 设置接受最终解析出来中转的数据监听器
     *
     * @param listener 监听器
     */
    public void setTransDataListener(IReceiverTransDataProxy listener) {
        this.mTransDataListener = listener;
    }

    /**
     * 设置自动响应的数据发送者
     *
     * @param sender 数据发送者
     */
    protected void setResponseSender(SecuritySender sender) {
        this.mResponseSender = sender;
    }

    /**
     * 实现安全协议解析流程
     */
    private class SecurityProtocolParserProcess implements ISecurityProtocolParserProcess {

        @Override
        public ByteBuffer decodeData(ByteBuffer encodeData) {
            boolean isServerMode = SecurityChannelContext.getInstance().isServerMode();
            if (!isServerMode) {
                //当前是客户端，接收到服务端返回数据则根据配置文件修改加解密方式
                String encryption = SecurityChannelContext.getInstance().getEncryption();
                EncryptionType encryptionType = EncryptionType.getInstance(encryption);
                mDataSafeManager.init(encryptionType);
            }
            byte[] data = encodeData.array();
            if (mDataSafeManager.isInit()) {
                byte[] decodeData = mDataSafeManager.decode(data);
                return ByteBuffer.wrap(decodeData);
            }
            return encodeData;
        }

        @Override
        public boolean onCheckTime(long time) {
            long nowTime = System.currentTimeMillis();
            //172800000 48小时的毫秒值
            return Math.abs(nowTime - time) < 172800000;
        }

        @Override
        public boolean onCheckMachineId(String machineId) {
            //检查mid是否合法
            mMachineId = machineId;
            List<String> machineList = SecurityChannelContext.getInstance().getMachineList();
            if (machineList != null) {
                for (String mid : machineList) {
                    if (mid.equalsIgnoreCase(machineId)) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }

        @Override
        public boolean onCheckChannelId(String channelId) {
            return mChannelId.equals(channelId);
        }


        /**
         * 创建通道id
         *
         * @return 返回通道id
         */
        private String createChannelId() {
            String uuidStr = UUID.randomUUID().toString();
            return Md5Helper.md5_32(uuidStr);
        }

        /**
         * 服务端处理init协议数据
         *
         * @param data 数据
         */
        private void serverExecInitCmd(ByteBuffer data) {
            //当前是处理客户端请求数据
            byte enType = data.get();
            //根据客户端定义初始化加解密
            EncryptionType encryption = EncryptionType.getInstance(enType);
            byte[] aesKey = null;
            if (encryption == EncryptionType.AES) {
                //获取AES对称密钥
                aesKey = new byte[data.limit() - data.position()];
                data.get(aesKey);
            }
            mResponseSender.changeEncryption(encryption, aesKey);
            //创建channel id 返回给客户端
            mChannelId = createChannelId();
            mResponseSender.setChannelId(mChannelId);
            LogDog.d("创建channel id 返回给客户端 " + mChannelId);
            //todo 暂时不实现-> 判断当前服务负载是否过高，如果过高返回低负载的服务地址
            mResponseSender.respondInitData(mMachineId, InitResult.NORMAL.getCode(), mChannelId.getBytes());
//        mSender.respondInitData(mMachineId, InitResult.SERVER_IP.getCode(), "127.0.0.1:5555".getBytes());
        }

        /**
         * 客户端处理init协议数据
         *
         * @param data 数据
         */
        private void clientExecInitCmd(ByteBuffer data) {
            //当前处理服务端响应数据
            byte resCode = data.get();
            InitResult status = InitResult.getInstance(resCode);
            byte[] context = new byte[data.limit() - data.position()];
            data.get(context);
            String contextStr = new String(context);
            if (InitResult.SERVER_IP == status) {
                //断开当前服务链接，连接返回的服务器ip
                String[] arrays = contextStr.split(":");
                if (arrays.length != 2) {
                    //返回的数据有异常,断开链接
                    throw new RuntimeException("The address data returned by channel is abnormal !!!");
                }
                SecurityChannelManager.getInstance().release();
                SecurityChannelManager.getInstance().init(arrays[0], Integer.parseInt(arrays[1]));
            } else if (InitResult.NORMAL == status) {
                //配置服务端返回的 channel id
                LogDog.d("接收到服务端返回的 channel id = " + contextStr);
                mResponseSender.setChannelId(contextStr);
            }
        }

        private String[] parseRequestAddress(byte[] requestHostByte) {
            if (requestHostByte == null) {
                return null;
            }
            //存在地址，说明是新的请求
            String requestHost = new String(requestHostByte);
            //链接真实的目标
            return requestHost.split(":");
        }

        private byte[] getContextData(ByteBuffer data) {
            int size = data.limit() - data.position();
            if (size <= 0) {
                return null;
            }
            byte[] context = new byte[size];
            data.get(context);
            return context;
        }

        @Override
        public void onExecCmd(byte cmd, ByteBuffer data) {
            boolean isServerMode = SecurityChannelContext.getInstance().isServerMode();
            if (cmd == CmdType.INIT.getCmd()) {
                if (isServerMode) {
                    serverExecInitCmd(data);
                } else {
                    clientExecInitCmd(data);
                }
            } else if (cmd == CmdType.TRANS.getCmd()) {
                //解析requestId
                byte[] requestIdByte = new byte[ProxyProtocol.REQUEST_LENGTH];
                data.get(requestIdByte);
                String requestId = new String(requestIdByte);
                if (StringEnvoy.isEmpty(requestId)) {
                    throw new RuntimeException("illegal requestId !!!");
                }
                //pctCount 作用于udp 传输数据 ，暂时不处理
                byte pctCount = data.get();
                byte[] context = getContextData(data);
                if (context == null) {
                    throw new RuntimeException("illegal trans context !!!");
                }
                if (mTransDataListener != null) {
                    mTransDataListener.onTransData(requestId, pctCount, context);
                }
            } else if (cmd == CmdType.REQUEST.getCmd()) {
                //解析requestId
                byte[] requestIdByte = new byte[ProxyProtocol.REQUEST_LENGTH];
                data.get(requestIdByte);
                String requestId = new String(requestIdByte);
                if (StringEnvoy.isEmpty(requestId)) {
                    throw new RuntimeException("illegal requestId !!!");
                }
                if (isServerMode) {
                    byte[] context = getContextData(data);
                    if (context == null) {
                        throw new RuntimeException("illegal request context !!!");
                    }
                    String[] realHost = parseRequestAddress(context);
                    if (realHost != null && realHost.length == 2) {
                        if (mTransDataListener != null) {
                            mTransDataListener.onCreateConnect(requestId, realHost[0], Integer.parseInt(realHost[1]));
                        }
                    } else {
                        throw new RuntimeException("illegal realHost !!!");
                    }
                } else {
                    byte status = data.get();
                    //表示服务端创建指定的目标链接成功
                    if (mTransDataListener != null) {
                        mTransDataListener.onCreateConnectStatus(requestId, status);
                    }
                }
            }
        }


        @Override
        public void onDeny(String msg) {
            LogDog.e(msg);
        }

        @Override
        public void onError() {

        }

    }
}
