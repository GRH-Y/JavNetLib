package com.jav.net.svp.bean;

import com.jav.net.svp.channel.ChannelInfo;
import com.jav.net.svp.protocol.ISvpProtocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PackMixBean {

    private ChannelInfo mChannelInfo;

    private final List<ByteBuffer> mPackDataList;

    public PackMixBean(ChannelInfo channelInfo) {
        if (mChannelInfo == null) {
            throw new IllegalArgumentException("channelInfo is null !");
        }
        mChannelInfo = channelInfo;
        mPackDataList = new ArrayList<>(mChannelInfo.getPackCount());
    }

    /**
     * 获取通道信息
     *
     * @return
     */
    public ChannelInfo getChannelInfo() {
        return mChannelInfo;
    }


    /**
     * 拼接数据包，根据索引值存放
     *
     * @param index     索引值
     * @param frameData 数据碎片
     */
    public void splicingPack(short index, ByteBuffer frameData) {
        checkIndex(index);
        checkData(frameData);
        frameData.rewind();
        mPackDataList.set(index, frameData);
    }

    /**
     * 提取整包索引值部分数据内容
     *
     * @param index 索引值
     * @return
     */
    public ByteBuffer extractFramePack(short index) {
        checkIndex(index);
        return mPackDataList.get(index);
    }

    /**
     * 提取完整的数据
     *
     * @return 完整数据
     */
    public ByteBuffer extractFullPack() {
        ByteBuffer fullData = null;
        if (isComplete()) {
            int maxSize = ISvpProtocol.DEFAULT_TRANSMIT_DATA_LENGTH * mChannelInfo.getPackCount();
            fullData = ByteBuffer.allocate(maxSize);
            for (ByteBuffer frame : mPackDataList) {
                fullData.put(frame);
            }
        }
        return fullData;
    }

    /**
     * 检查索引值
     *
     * @param index
     */
    private void checkIndex(short index) {
        if (index < 0 || index > mChannelInfo.getPackCount()) {
            throw new IllegalArgumentException("splicing Pack index cannot be less than 0");
        }
    }

    /**
     * 检查碎片数据
     *
     * @param frameData
     */
    private void checkData(ByteBuffer frameData) {
        if (frameData == null) {
            throw new IllegalArgumentException("splicing Pack frameData cannot be null !");
        }
    }

    /**
     * 判断是否收集完整的数据
     *
     * @return true表示完整
     */
    public boolean isComplete() {
        return mPackDataList.size() == mChannelInfo.getPackCount();
    }

    /**
     * 清除数据
     */
    public void clearData() {
        mPackDataList.clear();
    }

    /**
     * 释放资源
     */
    public void release() {
        clearData();
        mChannelInfo = null;
    }

}
