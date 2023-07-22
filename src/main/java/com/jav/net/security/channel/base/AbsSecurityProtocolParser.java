package com.jav.net.security.channel.base;


import com.jav.net.security.channel.joggle.ISecurityPolicyProcessor;
import com.jav.net.security.channel.joggle.ISecurityProtocolParser;

import java.nio.ByteBuffer;

/**
 * 安全协议解析器,主要解析协议数据
 *
 * @author yyz
 */
public abstract class AbsSecurityProtocolParser implements ISecurityProtocolParser {


    /**
     * 安全策略
     */
    protected ISecurityPolicyProcessor mPolicyProcessor;


    /**
     * 设置安全策略处理器
     *
     * @param processor
     */
    @Override
    public void setSecurityPolicyProcessor(ISecurityPolicyProcessor processor) {
        this.mPolicyProcessor = processor;
    }


    /**
     * 解析地址
     *
     * @param requestHostByte
     * @return
     */
    protected String[] parseRequestAddress(byte[] requestHostByte) {
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
    protected byte[] getContextData(ByteBuffer data) {
        int size = data.limit() - data.position();
        if (size <= 0) {
            return null;
        }
        byte[] context = new byte[size];
        data.get(context);
        return context;
    }


    /**
     * 解析校验时间字段
     *
     * @param remoteHost 远程的目标地址
     * @param decodeData 要校验的数据
     */
    protected void parseCheckTime(String remoteHost, ByteBuffer decodeData) {
        long time = decodeData.getLong();
        boolean isDeny = mPolicyProcessor.onCheckTime(time);
        if (!isDeny) {
            reportPolicyProcessor(remoteHost, UnusualBehaviorType.EXP_TIME);
        }
    }

    /**
     * 解析校验machine id字段
     *
     * @param remoteHost 远程的目标地址
     * @param decodeData 要校验的数据
     */
    protected String parseCheckMachineId(String remoteHost, ByteBuffer decodeData) {
        byte[] mid = new byte[ConstantCode.MACHINE_LENGTH];
        decodeData.get(mid);
        String machineIdStr = new String(mid);
        boolean isDeny = mPolicyProcessor.onCheckMachineId(machineIdStr);
        if (!isDeny) {
            reportPolicyProcessor(remoteHost, UnusualBehaviorType.EXP_MACHINE_ID);
        }
        return machineIdStr;
    }

    /**
     * 解析校验channel id
     *
     * @param decodeData 要校验的数据
     * @return true 校验通过
     */
    protected boolean parseCheckChannelId(ByteBuffer decodeData) {
        byte[] channelIdByte = new byte[ConstantCode.CHANNEL_LENGTH];
        decodeData.get(channelIdByte);
        String channelId = new String(channelIdByte);
        return mPolicyProcessor.onCheckChannelId(channelId);
    }

    /**
     * 反馈异常给安全策略处理器
     *
     * @param remoteHost 远程的目标地址
     * @param type       异常行为类型
     */
    @Override
    public void reportPolicyProcessor(String remoteHost, UnusualBehaviorType type) {
        if (mPolicyProcessor != null) {
            mPolicyProcessor.onUnusualBehavior(remoteHost, type);
        }
        throw new IllegalStateException(type.getErrorMsg());
    }


}
