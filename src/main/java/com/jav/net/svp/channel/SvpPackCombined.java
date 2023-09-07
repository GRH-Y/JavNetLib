package com.jav.net.svp.channel;

import com.jav.net.base.UdpPacket;
import com.jav.net.svp.bean.PackMixBean;
import com.jav.net.svp.channel.joggle.ICombinedPackCompleteListener;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * 根据pass id 记录数据包
 */
public class SvpPackCombined {

    private final HashMap<String, HashMap<Short, PackMixBean>> packMixCache;

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
        ByteBuffer fullData = packet.getFullData();
        byte[] passId = SvpDataParser.getPassId(fullData);
        String passIdStr = new String(passId);
        HashMap<Short, PackMixBean> target = packMixCache.get(passIdStr);
        short reqId = SvpDataParser.getReqId(fullData);
        PackMixBean packMixBean = null;
        if (target == null) {
            target = new HashMap<>();
            packMixCache.put(passIdStr, target);
        } else {
            packMixBean = target.get(reqId);
        }
        if (packMixBean == null) {
            long packId = SvpDataParser.getPackId(fullData);
            short packCount = SvpDataParser.getPackCount(fullData);
            packMixBean = new PackMixBean(reqId, packId, packCount);
            target.put(reqId, packMixBean);
            if (mCombinedPackCompleteListener != null) {
                byte[] adr = SvpDataParser.getAddress(fullData);
                int port = SvpDataParser.getPort(fullData);
                mCombinedPackCompleteListener.onRequestPack(adr, port);
            }
            return;
        }
        short index = SvpDataParser.getPackIndex(fullData);
        packMixBean.splicingPack(index, fullData);
        if (packMixBean.isComplete()) {
            if (mCombinedPackCompleteListener != null) {
                ByteBuffer fullPack = packMixBean.extractFullPack();
                mCombinedPackCompleteListener.onCombinedCompletePack(fullPack);
            }
            packMixBean.release();
            target.remove(reqId);
        }
    }

}
