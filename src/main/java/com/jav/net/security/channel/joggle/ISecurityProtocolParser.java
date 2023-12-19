package com.jav.net.security.channel.joggle;

import com.jav.net.security.channel.base.UnusualBehaviorType;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface ISecurityProtocolParser {

    /**
     * 设置安全策略处理器
     *
     * @param processor
     */
    void setSecurityPolicyProcessor(ISecurityPolicyProcessor processor);


    /**
     * 解析接受端的数据
     *
     * @param remoteHost 请求端的地址
     * @param decodeData 请求端发的数据
     */
    void parserReceiverData(InetSocketAddress remoteAddress, ByteBuffer decodeData);

    /**
     * 反馈异常给安全策略处理器
     *
     * @param remoteHost 远程的目标地址
     * @param type       异常行为类型
     */
    void reportPolicyProcessor(InetSocketAddress remoteAddress, UnusualBehaviorType type);

}
