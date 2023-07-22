package com.jav.net.security.channel;


import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.common.log.LogDog;
import com.jav.common.util.StringEnvoy;
import com.jav.net.security.channel.base.AbsSecurityProtocolParser;
import com.jav.net.security.channel.base.ConstantCode;
import com.jav.net.security.channel.base.ParserCallBackRegistrar;
import com.jav.net.security.channel.base.UnusualBehaviorType;
import com.jav.net.security.channel.joggle.IChannelEventCallBack;
import com.jav.net.security.channel.joggle.IClientEventCallBack;
import com.jav.net.security.channel.joggle.IServerEventCallBack;
import com.jav.net.security.protocol.base.ActivityCode;
import com.jav.net.security.protocol.base.InitResult;
import com.jav.net.security.protocol.base.TransOperateCode;

import java.nio.ByteBuffer;

/**
 * 安全协议解析器,主要解析协议数据
 *
 * @author yyz
 */
public class SecurityCommProtocolParser extends AbsSecurityProtocolParser {

    private final SecurityChannelContext mContext;

    /**
     * 解析结果的回调监听器
     */
    protected ParserCallBackRegistrar mCallBackRegistrar;


    public SecurityCommProtocolParser(SecurityChannelContext context) {
        mContext = context;
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
        if (InitResult.ERROR == result) {
            throw new RuntimeException(UnusualBehaviorType.EXP_INIT_DATA.getErrorMsg());
        }
        byte[] context = new byte[data.limit() - data.position()];
        data.get(context);
        String contextStr = new String(context);
        if (InitResult.SERVER_IP == result) {
            // 断开当前服务链接，连接返回的服务器ip
            String[] arrays = contextStr.split(":");
            if (arrays.length != ConstantCode.NORMAL_ADDRESS_LENGTH) {
                // 返回的数据有异常,断开链接
                throw new RuntimeException(UnusualBehaviorType.EXP_ADDRESS_LENGTH.getErrorMsg());
            }
            if (mCallBackRegistrar != null) {
                IClientEventCallBack callBack = mCallBackRegistrar.getClientCallBack();
                if (callBack != null) {
                    callBack.onRespondServerHighLoadCallBack(arrays[0], Integer.parseInt(arrays[1]));
                }
            }
        } else if (InitResult.CHANNEL_ID == result) {
            // 配置服务端返回的 channel id
            LogDog.d("接收到服务端返回的 channel id = " + contextStr);
            if (mCallBackRegistrar != null) {
                IClientEventCallBack callBack = mCallBackRegistrar.getClientCallBack();
                if (callBack != null) {
                    callBack.onRespondChannelIdCallBack(contextStr);
                }
            }
        }
    }


    /**
     * 解析接受端的数据
     *
     * @param remoteHost
     * @param decodeData
     */
    @Override
    public void parserReceiverData(String remoteHost, ByteBuffer decodeData) {
        // 解析校验时间字段
        parseCheckTime(remoteHost, decodeData);
        // 解析cmd字段
        byte cmd = decodeData.get();

        String machineId = null;
        if (cmd == ActivityCode.INIT.getCode()) {
            // 解析校验machine id字段
            machineId = parseCheckMachineId(remoteHost, decodeData);
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
            reportPolicyProcessor(remoteHost, UnusualBehaviorType.EXP_ACTIVITY);
        }
        byte oCode = decodeData.get();
        parserActivityForData(cmd, oCode, machineId, decodeData);
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
                    callBack.onRespondRequestStatusCallBack(requestId, status);
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
