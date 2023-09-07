package com.jav.net.svp.bean;

import com.jav.net.svp.protocol.ISvpProtocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PackMixBean {

    private short mRequestId;

    private long mPackId;

    private short mPackCount;

    private final List<ByteBuffer> mPackDataList;


    public PackMixBean(short requestId, long packId, short packCount) {
        mRequestId = requestId;
        mPackId = packId;
        mPackCount = packCount;
        mPackDataList = new ArrayList<>(mPackCount);
    }

    /**
     * 获取req id
     *
     * @return
     */
    public short getRequestId() {
        return mRequestId;
    }

    /**
     * 获取pack id
     *
     * @return
     */
    public long getPackId() {
        return mPackId;
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
            int maxSize = ISvpProtocol.DEFAULT_TRANSMIT_DATA_LENGTH * mPackCount;
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
        if (index < 0) {
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
        return mPackDataList.size() == mPackCount;
    }

    /**
     * 释放资源
     */
    public void release() {
        mPackDataList.clear();
        mPackId = -1;
        mRequestId = -1;
        mPackCount = -1;
    }

}
