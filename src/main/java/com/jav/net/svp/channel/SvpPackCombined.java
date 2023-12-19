package com.jav.net.svp.channel;

import com.jav.common.log.LogDog;
import com.jav.net.base.UdpPacket;
import com.jav.net.svp.bean.PackMixBean;
import com.jav.net.svp.channel.joggle.ICombinedPackCompleteListener;
import com.jav.net.svp.protocol.SvpFlags;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * 根据pass id 记录数据包
 */
public class SvpPackCombined {

    /**
     * 缓存数据包，用于拼接完整的包
     * 数据结构 passId -> reqId -> packId -> pack
     */
    private final HashMap<String, HashMap<Short, HashMap<Long, PackMixBean>>> packMixCache;

    private final ICombinedPackCompleteListener mCombinedPackCompleteListener;

    public SvpPackCombined(ICombinedPackCompleteListener listener) {
        mCombinedPackCompleteListener = listener;
        packMixCache = new HashMap<>();
    }

    /**
     * 接收碎片的数据包，根据pass id 和 req id 缓存到对应的队列中，接收完所有数据包再回调到监听器处理
     *
     * @param packet 碎片数据包
     */
    public void combinedPack(UdpPacket packet) {
        ByteBuffer[] allData = packet.getUdpData().getDirtyBuf();
        ByteBuffer fullData = allData[0];
        byte[] passId = SvpDataParser.getPassId(fullData);
        String passIdStr = new String(passId);
        HashMap<Short, HashMap<Long, PackMixBean>> targetMachine = packMixCache.get(passIdStr);
        short reqId = SvpDataParser.getReqId(fullData);
        HashMap<Long, PackMixBean> targetRequest = null;
        if (targetMachine == null) {
            targetMachine = new HashMap<>();
            packMixCache.put(passIdStr, targetMachine);
        } else {
            targetRequest = targetMachine.get(reqId);
        }
        long packId = SvpDataParser.getPackId(fullData);
        PackMixBean targetPack;
        ChannelInfo channelInfo;
        if (targetRequest == null) {
            targetRequest = new HashMap<>();
            byte flags = SvpDataParser.getFlags(fullData);
            boolean isTcp = (SvpFlags.TCP & flags) == SvpFlags.TCP;
            short packCount = SvpDataParser.getPackCount(fullData);
            byte[] adr = SvpDataParser.getAddress(fullData);
            int port = SvpDataParser.getPort(fullData);

            channelInfo = new ChannelInfo();
            channelInfo.setPackCount(packCount);
            channelInfo.setPassId(passIdStr);
            channelInfo.setRequestId(reqId);
            channelInfo.setPackId(packId);
            channelInfo.setTcp(isTcp);
            channelInfo.setTargetAddress(new InetSocketAddress(new String(adr), port));
            channelInfo.setSourceAddress(packet.getAddress());
            LogDog.i("#svp# source adr = " + channelInfo.getSourceAddress().toString()
                    + "target adr = " + channelInfo.getTargetAddress().toString());

            targetPack = new PackMixBean(channelInfo);
            targetRequest.put(packId, targetPack);
            targetMachine.put(reqId, targetRequest);
            if (mCombinedPackCompleteListener != null) {
                //通知上层，开始创建目标连接
                mCombinedPackCompleteListener.onRequestPack(channelInfo);
            }
            return;
        }

        targetPack = targetRequest.get(packId);
        channelInfo = targetPack.getChannelInfo();

        short index = SvpDataParser.getPackIndex(fullData);
        targetPack.splicingPack(index, fullData);
        LogDog.i("#svp# splicing pack [ passId:" + passIdStr + " reqId:" + reqId +
                " packId:" + packId + " index:" + index + " ]");
        if (targetPack.isComplete()) {
            if (mCombinedPackCompleteListener != null) {
                ByteBuffer fullPack = targetPack.extractFullPack();
                LogDog.i("#svp# complete pack !");
                mCombinedPackCompleteListener.onCombinedCompletePack(channelInfo, fullPack);
            }
            targetPack.clearData();
            targetMachine.remove(reqId);
        }
    }

    /**
     * 一般接收到init 协议清除上一条记录
     *
     * @param channelInfo
     */
    public void clearChannelInfo(ChannelInfo channelInfo) {
        if (channelInfo == null) {
            return;
        }
        String passIdStr = channelInfo.getPassId();
        packMixCache.remove(passIdStr);
    }

    /**
     * reqId 是用于区分不同目标或者同目标不同次数的请求，一般等目标链接结束调用清理
     *
     * @param channelInfo
     */
    public void clearChannelRequestInfo(ChannelInfo channelInfo) {
        if (channelInfo == null) {
            return;
        }
        String passIdStr = channelInfo.getPassId();
        short reqId = channelInfo.getRequestId();
        HashMap<Short, HashMap<Long, PackMixBean>> targetMachine = packMixCache.get(passIdStr);
        if (targetMachine == null) {
            return;
        }
        targetMachine.remove(reqId);
    }

}
