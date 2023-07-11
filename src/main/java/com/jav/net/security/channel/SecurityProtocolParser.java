package com.jav.net.security.channel;


import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.log.LogDog;
import com.jav.common.util.StringEnvoy;
import com.jav.net.security.cache.CacheExtMachineIdMater;
import com.jav.net.security.channel.base.ConstantCode;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.base.UnusualBehaviorType;
import com.jav.net.security.channel.joggle.*;
import com.jav.net.security.protocol.base.ActivityCode;
import com.jav.net.security.protocol.base.InitResult;
import com.jav.net.security.protocol.base.SyncOperateCode;
import com.jav.net.security.protocol.base.TransOperateCode;

import java.nio.ByteBuffer;

/**
 * 安全协议解析器,主要解析协议数据
 *
 * @author yyz
 */
public class SecurityProtocolParser {


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
    private void serverExecInitCmd(byte enType, String machineId, ByteBuffer data) {
        // 根据客户端定义初始化加解密
        EncryptionType encryption = EncryptionType.getInstance(enType);
        byte[] aesKey = null;
        if (encryption == EncryptionType.AES) {
            // 获取AES对称密钥
            aesKey = new byte[data.limit() - data.position()];
            data.get(aesKey);
        }

        if (mCallBackRegistrar != null) {
            IServerEventCallBack callBack = mCallBackRegistrar.getServerCallBack();
            if (callBack != null) {
                callBack.onInitForServerCallBack(encryption, aesKey, machineId);
            }
        }
    }

    /**
     * 客户端处理init协议数据
     *
     * @param data 数据
     */
    private void clientExecInitCmd(byte oCode, ByteBuffer data) {
        byte realOperateCode = (byte) (oCode & (~ConstantCode.REP_EXCEPTION_CODE));
        byte status = (byte) (oCode & ConstantCode.REP_EXCEPTION_CODE);
        if (status == ConstantCode.REP_EXCEPTION_CODE) {
            throw new RuntimeException(UnusualBehaviorType.EXP_INIT_DATA.getErrorMsg());
        }
        // 当前处理服务端响应数据
        InitResult result = InitResult.getInstance(realOperateCode);
        byte[] context = new byte[data.limit() - data.position()];
        data.get(context);
        String contextStr = new String(context);
        if (InitResult.SERVER_IP == result) {
            // 断开当前服务链接，连接返回的服务器ip
            String[] arrays = contextStr.split(":");
            if (arrays.length != ConstantCode.NORMAL_ADDRESS_LENGTH) {
                // 返回的数据有异常,断开链接
                throw new RuntimeException(UnusualBehaviorType.EXP_LENGTH.getErrorMsg());
            }
            SecurityChannelManager.getInstance().release();
            SecurityChannelManager.getInstance().resetConnectLowLoadServer(arrays[0], Integer.parseInt(arrays[1]));
        } else if (InitResult.CHANNEL_ID == result) {
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

    /**
     * 解析地址
     *
     * @param requestHostByte
     * @return
     */
    private String[] parseRequestAddress(byte[] requestHostByte) {
        if (requestHostByte == null) {
            return null;
        }
        // 存在地址，说明是新的请求
        String requestHost = new String(requestHostByte);
        // 链接真实的目标
        return requestHost.split(":");
    }

    /**
     * 获取数据本体
     *
     * @param data
     * @return
     */
    private byte[] getContextData(ByteBuffer data) {
        int size = data.limit() - data.position();
        if (size <= 0) {
            return null;
        }
        byte[] context = new byte[size];
        data.get(context);
        return context;
    }

    /**
     * 解析接受端的数据
     *
     * @param remoteHost
     * @param decodeData
     */
    public void parserReceiverData(String remoteHost, ByteBuffer decodeData) {
        // 解析校验时间字段
        parseCheckTime(remoteHost, decodeData);
        // 解析cmd字段
        byte cmd = decodeData.get();

        String machineId = null;
        if (cmd == ActivityCode.INIT.getCode() || cmd == ActivityCode.SYNC.getCode()) {
            // 解析校验machine id字段
            machineId = parseCheckMachineId(remoteHost, decodeData);
            if (cmd == ActivityCode.SYNC.getCode()) {
                if (!machineId.startsWith("s")) {
                    throw new IllegalStateException(UnusualBehaviorType.EXP_SYNC_MACHINE_ID.getErrorMsg());
                }
            }
        } else if (cmd == ActivityCode.TRANS.getCode() || cmd == ActivityCode.KEEP.getCode()) {
            boolean isOk = parseCheckChannelId(decodeData);
            if (!isOk) {
                //channel id 异常, 断开链接
                throw new IllegalStateException(UnusualBehaviorType.EXP_CHANNEL_ID.getErrorMsg());
            }
            if (cmd == ActivityCode.KEEP.getCode()) {
                //心跳协议不需要处理任何数据
                return;
            }
        } else {
            callBackDeny(remoteHost, UnusualBehaviorType.EXP_ACTIVITY);
        }
        byte oCode = decodeData.get();
        parserActivityForData(cmd, oCode, machineId, decodeData);
    }


    /**
     * 解析校验时间字段
     *
     * @param remoteHost 远程的目标地址
     * @param decodeData 要校验的数据
     */
    private void parseCheckTime(String remoteHost, ByteBuffer decodeData) {
        long time = decodeData.getLong();
        boolean isDeny = mPolicyProcessor.onCheckTime(time);
        if (!isDeny) {
            callBackDeny(remoteHost, UnusualBehaviorType.EXP_TIME);
        }
    }

    /**
     * 解析校验machine id字段
     *
     * @param remoteHost 远程的目标地址
     * @param decodeData 要校验的数据
     */
    private String parseCheckMachineId(String remoteHost, ByteBuffer decodeData) {
        byte[] mid = new byte[ConstantCode.MACHINE_LENGTH];
        decodeData.get(mid);
        String machineIdStr = new String(mid);
        boolean isDeny = mPolicyProcessor.onCheckMachineId(machineIdStr);
        if (!isDeny) {
            callBackDeny(remoteHost, UnusualBehaviorType.EXP_MACHINE_ID);
        }
        return machineIdStr;
    }

    /**
     * 解析校验channel id
     *
     * @param decodeData 要校验的数据
     * @return true 校验通过
     */
    private boolean parseCheckChannelId(ByteBuffer decodeData) {
        byte[] channelIdByte = new byte[ConstantCode.CHANNEL_LENGTH];
        decodeData.get(channelIdByte);
        String channelId = new String(channelIdByte);
        return mPolicyProcessor.onCheckChannelId(channelId);
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
        throw new IllegalStateException(type.getErrorMsg());
    }


    /**
     * 根据不同的activity分别处理
     *
     * @param activity
     * @param oCode
     * @param machineId
     * @param data
     */
    private void parserActivityForData(byte activity, byte oCode, String machineId, ByteBuffer data) {
        if (activity == ActivityCode.INIT.getCode()) {
            if (mContext.isServerMode()) {
                serverExecInitCmd(oCode, machineId, data);
            } else {
                clientExecInitCmd(oCode, data);
            }
        } else if (activity == ActivityCode.TRANS.getCode()) {
            parserTrans(oCode, data);
        } else if (activity == ActivityCode.SYNC.getCode()) {
            parserSync(oCode, machineId, data);
        }
    }

    /**
     * 解析sync 行为
     *
     * @param oCode
     * @param machineId
     * @param data
     */
    private void parserSync(byte oCode, String machineId, ByteBuffer data) {
        byte realOperateCode = (byte) (oCode & (~ConstantCode.REP_EXCEPTION_CODE));
        if (realOperateCode == SyncOperateCode.SYNC_AVG.getCode()) {
            if (mCallBackRegistrar != null) {
                ISyncServerEventCallBack callBack = mCallBackRegistrar.getSyncCallBack();
                if (callBack != null) {
                    int proxyPort = data.getInt();
                    long loadData = data.getLong();
                    byte status = (byte) (oCode & ConstantCode.REP_EXCEPTION_CODE);
                    callBack.onRespondSyncCallBack(status, proxyPort, machineId, loadData);
                    LogDog.w("## receive sync avg data , server machine id : " + machineId
                            + " proxyPort : " + proxyPort + " loadData : " + loadData);
                }
            }
        } else if (realOperateCode == SyncOperateCode.SYNC_MID.getCode()) {
            byte[] context = getContextData(data);
            if (context == null) {
                throw new RuntimeException(UnusualBehaviorType.EXP_SYNC_DATA.getErrorMsg());
            }
            String clientMachineId = new String(context);
            if (mContext.isServerMode()) {
                //添加到 mid 列表里
                boolean ret = CacheExtMachineIdMater.getInstance().cacheMid(clientMachineId);
                LogDog.w("## cache client machine id : " + clientMachineId);
                byte status = ret ? ConstantCode.REP_SUCCESS_CODE : ConstantCode.REP_EXCEPTION_CODE;
                //响应同步mid结果
                SecurityServerSyncImage.getInstance().respondSyncMid(mContext.getMachineId(), clientMachineId, status);
                LogDog.w("## start respond sync data , server machine id : " + machineId + " status : " + ret);
            } else {
                //通知 init 接口添加mid成功，可以切换中转服务
                if (mCallBackRegistrar != null) {
                    ISyncServerEventCallBack callBack = mCallBackRegistrar.getSyncCallBack();
                    if (callBack != null) {
                        byte status = (byte) (oCode & ConstantCode.REP_EXCEPTION_CODE);
                        callBack.onRespondSyncMidCallBack(status, clientMachineId);
                    }
                }
            }
        } else {
            throw new RuntimeException(UnusualBehaviorType.EXP_OPERATE_CODE.getErrorMsg());
        }
    }

    /**
     * 解析trans的行为数据
     *
     * @param oCode
     * @param data
     */
    private void parserTrans(byte oCode, ByteBuffer data) {
        // 解析requestId
        byte[] requestIdByte = new byte[ConstantCode.REQUEST_LENGTH];
        data.get(requestIdByte);
        String requestId = new String(requestIdByte);
        if (StringEnvoy.isEmpty(requestId)) {
            throw new RuntimeException(UnusualBehaviorType.EXP_REQUEST_ID.getErrorMsg());
        }
        byte[] context = getContextData(data);
        byte realOperateCode = (byte) (oCode & (~ConstantCode.REP_EXCEPTION_CODE));
        if (realOperateCode == TransOperateCode.ADDRESS.getCode()) {
            notifyTransAddress(oCode, requestId, context);
        } else if (realOperateCode == TransOperateCode.DATA.getCode()) {
            if (context == null) {
                throw new RuntimeException(UnusualBehaviorType.EXP_TRANS_DATA.getErrorMsg());
            }
            notifyTransData(oCode, requestId, context);
        } else {
            throw new RuntimeException(UnusualBehaviorType.EXP_OPERATE_CODE.getErrorMsg());
        }
    }

    /**
     * 分发trans的address类型的数据
     *
     * @param oCode     操作码
     * @param requestId 请求id
     * @param context   数据内容
     */
    private void notifyTransAddress(byte oCode, String requestId, byte[] context) {
        if (mContext.isServerMode()) {
            if (context == null) {
                throw new RuntimeException(UnusualBehaviorType.EXP_TRANS_DATA.getErrorMsg());
            }
            String[] realHost = parseRequestAddress(context);
            if (realHost.length == ConstantCode.NORMAL_ADDRESS_LENGTH) {
                if (mCallBackRegistrar != null) {
                    IServerEventCallBack callBack = mCallBackRegistrar.getServerCallBack();
                    if (callBack != null) {
                        callBack.onConnectTargetCallBack(requestId, realHost[0], Integer.parseInt(realHost[1]));
                    }
                }
            } else {
                IServerEventCallBack callBack = mCallBackRegistrar.getServerCallBack();
                if (callBack != null) {
                    callBack.onChannelError(UnusualBehaviorType.EXP_TRANS_ADDRESS, null);
                }
            }
        } else {
            // 表示服务端创建指定的目标链接成功
            if (mCallBackRegistrar != null) {
                IClientEventCallBack callBack = mCallBackRegistrar.getClientCallBack();
                if (callBack != null) {
                    byte status = (byte) (oCode & ConstantCode.REP_EXCEPTION_CODE);
                    callBack.onConnectTargetStatusCallBack(requestId, status);
                }
            }
        }
    }

    /**
     * 分发trans的data类型的数据
     *
     * @param requestId 请求id
     * @param context   数据内容
     */
    private void notifyTransData(byte oCode, String requestId, byte[] context) {
        if (mContext.isServerMode()) {
            if (mCallBackRegistrar == null) {
                return;
            }
            IChannelEventCallBack callBack = mCallBackRegistrar.getServerCallBack();
            callBack.onTransData(requestId, context);
        } else {
            byte status = (byte) (oCode & ConstantCode.REP_EXCEPTION_CODE);
            if (status == ConstantCode.REP_EXCEPTION_CODE) {
                throw new RuntimeException(UnusualBehaviorType.EXP_TRANS_DATA.getErrorMsg());
            }
            if (mCallBackRegistrar == null) {
                return;
            }
            IChannelEventCallBack callBack = mCallBackRegistrar.getClientCallBack();
            callBack.onTransData(requestId, context);
        }
    }


}
