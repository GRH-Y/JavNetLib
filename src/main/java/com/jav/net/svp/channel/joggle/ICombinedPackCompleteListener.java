package com.jav.net.svp.channel.joggle;


import java.nio.ByteBuffer;

/**
 * 接收者完成拼接完成完整的数据包回调监听
 *
 * @author no.8
 */
public interface ICombinedPackCompleteListener {

    /**
     * 接收到request包，收到该包需要创建链接目标通路
     *
     * @param adr  目标的ip地址
     * @param port 目标端口号
     */
    void onRequestPack(byte[] adr, int port);

    /**
     * 回调完整的数据包
     *
     * @param pack
     */
    void onCombinedCompletePack(ByteBuffer pack);
}
