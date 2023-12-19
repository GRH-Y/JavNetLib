package com.jav.net.svp.channel.joggle;


import com.jav.net.svp.channel.ChannelInfo;

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
     * @param channelInfo 通道的信息
     */
    void onRequestPack(ChannelInfo channelInfo);

    /**
     * 回调完整的数据包
     *
     * @param channelInfo
     */
    void onCombinedCompletePack(ChannelInfo channelInfo, ByteBuffer fullData);
}
