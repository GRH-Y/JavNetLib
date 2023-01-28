package com.jav.net.security.channel;


import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.log.LogDog;
import com.jav.common.util.StringEnvoy;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.joggle.*;
import com.jav.net.security.protocol.AbsProxyProtocol;

import java.nio.ByteBuffer;

/**
 * 安全协议解析器,主要解析协议数据
 *
 * @author yyz
 */
public class SecurityProtocolParser {

    private final static int NORMAL_ADDRESS_LENGTH = 2;

    private final SecurityChannelContext mContext;

    /**
     * 安全策略
     */
    private ISecurityPolicyProcessor mPolicyProcessor;

    /**
     * 解析结果的回调监听器
     */
    private ParserCallBackRegistrar mCallBackRegistrar;


    public SecurityProtocolParser(SecurityChannelContext context) {
        mContext = context;
    }


    /**
     * 设置安全策略处理器
     *
     * @param processor
     */
    public void setSecurityPolicyProcessor(ISecurityPolicyProcessor processor) {
        this.mPolicyProcessor = processor;
    }

    /**
     * 设置接受最终解析出来中转的数据监听器
     *
     * @param registrar 监听器
     */
    public void setProtocolParserCallBack(ParserCallBackRegistrar registrar) {
        this.mCallBackRegistrar = registrar;
    }


    /**
     * 服务端处理init协议数据
     *
     * @param data 数据
     */
    private void serverExecInitCmd(String machineId, ByteBuffer data) {
        // 当前是处理客户端请求数据
        byte enType = data.get();
        // 根据客户端定义初始化加解密
        EncryptionType encryption = EncryptionType.getInstance(enType);
        byte[] aesKey = null;
        if (encryption == EncryptionType.AES) {
            // 获取AES对称密钥
            aesKey = new byte[data.limit() - data.position()];
            data.get(aesKey);
        }

        // 创建channel id 返回给客户端
        String channelId = mPolicyProcessor.createChannelId(machineId);

        LogDog.d("创建channel id 返回给客户端 " + channelId);

        if (mCallBackRegistrar != null) {
            IServerEventCallBack callBack = mCallBackRegistrar.getServerCallBack();
            if (callBack != null) {
                callBack.onInitForServerCallBack(encryption, aesKey, machineId, channelId);
            }
        }
    }

    /**
     * 客户端处理init协议数据
     *
     * @param data 数据
     */
    private void clientExecInitCmd(ByteBuffer data) {
        // 当前处理服务端响应数据
        byte resCode = data.get();
        InitResult status = InitResult.getInstance(resCode);
        byte[] context = new byte[data.limit() - data.position()];
        data.get(context);
        String contextStr = new String(context);
        if (InitResult.SERVER_IP == status) {
            // 断开当前服务链接，连接返回的服务器ip
            String[] arrays = contextStr.split(":");
            if (arrays.length != NORMAL_ADDRESS_LENGTH) {
                // 返回的数据有异常,断开链接
                throw new RuntimeException("The address data returned by channel is abnormal !!!");
            }
            SecurityChannelManager.getInstance().release();
            SecurityChannelManager.getInstance().resetConnectLowLoadServer(arrays[0], Integer.parseInt(arrays[1]));
        } else if (InitResult.CHANNEL_ID == status) {
            // 配置服务端返回的 channel id
            LogDog.d("接收到服务端返回的 channel id = " + contextStr);
            if (mCallBackRegistrar != null) {
                IClientEventCallBack callBack = mCallBackRegistrar.getClientCallBack();
                if (callBack != null) {
                    callBack.onInitForClientCallBack(contextStr);
                }
            }
        }
    }

    private String[] parseRequestAddress(byte[] requestHostByte) {
        if (requestHostByte == null) {
            return null;
        }
        // 存在地址，说明是新的请求
        String requestHost = new String(requestHostByte);
        // 链接真实的目标
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

    public void parserData(String remoteHost, ByteBuffer decodeData) {
        // 解析校验时间字段
        parseTime(remoteHost, decodeData);
        // 解析cmd字段
        byte cmd = decodeData.get();
        String machineId = null;
        String channelId = null;
        if (cmd == CmdType.INIT.getCmd() || cmd == CmdType.SYNC.getCmd()) {
            // 解析校验machine id字段
            machineId = parseMachineId(remoteHost, decodeData);
        } else if (cmd == CmdType.REQUEST.getCmd() || cmd == CmdType.TRANS.getCmd()) {
            channelId = parseChannelId(remoteHost, decodeData);
        }
        parserCmdForData(cmd, machineId, channelId, decodeData);
    }


    /**
     * 解析校验时间字段
     *
     * @param remoteHost 远程的目标地址
     * @param decodeData 要校验的数据
     */
    private void parseTime(String remoteHost, ByteBuffer decodeData) {
        long time = decodeData.getLong();
        boolean isDeny = mPolicyProcessor.onCheckTime(time);
        if (!isDeny) {
            callBackDeny(remoteHost, UnusualBehaviorType.TIME);
        }
    }

    /**
     * 解析校验machine id字段
     *
     * @param remoteHost 远程的目标地址
     * @param decodeData 要校验的数据
     */
    private String parseMachineId(String remoteHost, ByteBuffer decodeData) {
        byte[] mid = new byte[AbsProxyProtocol.MACHINE_LENGTH];
        decodeData.get(mid);
        String machineIdStr = new String(mid);
        boolean isDeny = mPolicyProcessor.onCheckMachineId(machineIdStr);
        if (!isDeny) {
            callBackDeny(remoteHost, UnusualBehaviorType.MACHINE);
        }
        return machineIdStr;
    }

    /**
     * 解析校验channel id
     *
     * @param remoteHost 远程的目标地址
     * @param decodeData 要校验的数据
     * @return 返回 channel id
     */
    private String parseChannelId(String remoteHost, ByteBuffer decodeData) {
        byte[] channelIdByte = new byte[AbsProxyProtocol.CHANNEL_LENGTH];
        decodeData.get(channelIdByte);
        String channelId = new String(channelIdByte);
        boolean isDeny = mPolicyProcessor.onCheckChannelId(channelId);
        if (!isDeny) {
            callBackDeny(remoteHost, UnusualBehaviorType.CHANNEL);
        }
        return channelId;
    }

    /**
     * 回调数据异常拒绝访问
     *
     * @param remoteHost 远程的目标地址
     * @param type       异常行为类型
     */
    public void callBackDeny(String remoteHost, UnusualBehaviorType type) {
        if (mPolicyProcessor != null) {
            mPolicyProcessor.onUnusualBehavior(remoteHost, type);
        }
        throw new IllegalStateException(type.getError());
    }


    private void parserCmdForData(byte cmd, String machineId, String channelId, ByteBuffer data) {
        if (cmd == CmdType.INIT.getCmd()) {
            if (mContext.isServerMode()) {
                serverExecInitCmd(machineId, data);
            } else {
                clientExecInitCmd(data);
            }
        } else if (cmd == CmdType.TRANS.getCmd()) {
            // 解析requestId
            byte[] requestIdByte = new byte[AbsProxyProtocol.REQUEST_LENGTH];
            data.get(requestIdByte);
            String requestId = new String(requestIdByte);
            if (StringEnvoy.isEmpty(requestId)) {
                throw new RuntimeException("illegal requestId !!!");
            }
            // pctCount 作用于udp 传输数据 ，暂时不处理
            byte pctCount = data.get();
            byte[] context = getContextData(data);
            if (context == null) {
                throw new RuntimeException("illegal trans context !!!");
            }
            if (mCallBackRegistrar != null) {
                ITransDataCallBack callBack = mCallBackRegistrar.getClientCallBack();
                if (mContext.isServerMode()) {
                    callBack = mCallBackRegistrar.getServerCallBack();
                }
                callBack.onTransData(requestId, pctCount, context);
            }
        } else if (cmd == CmdType.REQUEST.getCmd()) {
            // 解析requestId
            byte[] requestIdByte = new byte[AbsProxyProtocol.REQUEST_LENGTH];
            data.get(requestIdByte);
            String requestId = new String(requestIdByte);
            if (StringEnvoy.isEmpty(requestId)) {
                throw new RuntimeException("illegal requestId !!!");
            }
            if (mContext.isServerMode()) {
                byte[] context = getContextData(data);
                if (context == null) {
                    throw new RuntimeException("illegal request context !!!");
                }
                String[] realHost = parseRequestAddress(context);
                if (realHost.length == NORMAL_ADDRESS_LENGTH) {
                    if (mCallBackRegistrar != null) {
                        IServerEventCallBack callBack = mCallBackRegistrar.getServerCallBack();
                        if (callBack != null) {
                            callBack.onConnectTargetCallBack(requestId, realHost[0], Integer.parseInt(realHost[1]));
                        }
                    }
                } else {
                    throw new RuntimeException("illegal realHost !!!");
                }
            } else {
                byte status = data.get();
                // 表示服务端创建指定的目标链接成功
                if (mCallBackRegistrar != null) {
                    IClientEventCallBack callBack = mCallBackRegistrar.getClientCallBack();
                    if (callBack != null) {
                        callBack.onConnectTargetStatusCallBack(requestId, status);
                    }
                }
            }
        } else if (cmd == CmdType.SYNC.getCmd()) {
            if (mContext.isServerMode()) {
                byte status = data.get();
                int proxyPort = data.getInt();
                long loadData = data.getLong();
                if (mCallBackRegistrar != null) {
                    ISyncServerEventCallBack callBack = mCallBackRegistrar.getSyncCallBack();
                    if (callBack != null) {
                        callBack.onRespondSyncCallBack(status, proxyPort, machineId, loadData);
                    }
                }
            }
        }
    }


}
