package com.jav.net.security.channel.base;


import com.jav.net.security.channel.joggle.ISecurityPolicyProcessor;
import com.jav.net.security.channel.joggle.ISecurityProtocolParser;
import com.jav.net.security.guard.SecurityMachineIdMonitor;

import java.net.InetSocketAddress;
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
     * @param remoteAddress 远程的目标地址
     * @param decodeData    要校验的数据
     */
    protected void parseCheckTime(InetSocketAddress remoteAddress, ByteBuffer decodeData) {
        long time = decodeData.getLong();
        boolean isNotDeny = mPolicyProcessor.onCheckTime(time);
        if (!isNotDeny) {
            reportPolicyProcessor(remoteAddress, UnusualBehaviorType.EXP_TIME);
        }
    }

    /**
     * 解析校验machine id字段
     *
     * @param remoteAddress 远程的目标地址
     * @param decodeData    要校验的数据
     */
    protected String parseCheckMachineId(InetSocketAddress remoteAddress, ByteBuffer decodeData) {
        byte[] mid = new byte[ConstantCode.MACHINE_LENGTH];
        decodeData.get(mid);
        String machineIdStr = new String(mid);
        boolean isNotDeny = mPolicyProcessor.onCheckMachineId(machineIdStr);
        if (!isNotDeny) {
            reportPolicyProcessor(remoteAddress, UnusualBehaviorType.EXP_MACHINE_ID);
        }
        return machineIdStr;
    }

    /**
     * 解析校验machine id字段是否被多台机器同时使用
     *
     * @param remoteAddress 远程的目标地址
     * @param decodeData    要校验的数据
     */
    protected String parseCheckRepeatMachineId(InetSocketAddress remoteAddress, ByteBuffer decodeData) {
        byte[] mid = new byte[ConstantCode.MACHINE_LENGTH];
        decodeData.get(mid);
        String machineId = new String(mid);
        String address = remoteAddress.getHostName();
        boolean isNotValid = SecurityMachineIdMonitor.getInstance().checkMachineIdForAddress(machineId, address);
        if (!isNotValid) {
            reportPolicyProcessor(remoteAddress, UnusualBehaviorType.EXP_REPEAT_CODE);
        }
        return machineId;
    }

    /**
     * 反馈异常给安全策略处理器
     *
     * @param remoteAddress 远程的目标地址
     * @param type          异常行为类型
     */
    @Override
    public void reportPolicyProcessor(InetSocketAddress remoteAddress, UnusualBehaviorType type) {
        if (mPolicyProcessor != null) {
            mPolicyProcessor.onUnusualBehavior(remoteAddress.getHostName(), type);
        }
        throw new IllegalStateException(type.getErrorMsg());
    }


}
