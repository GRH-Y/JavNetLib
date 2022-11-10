package com.jav.net.component;

import com.jav.common.util.MultiplexCache;
import com.jav.net.component.joggle.IBufferComponent;
import com.jav.net.entity.MultiByteBuffer;

/**
 * ByteBuffer
 *
 * @author yyz
 */
public class CacheByteBufferComponent implements IBufferComponent<MultiByteBuffer> {

    private final MultiplexCache<MultiByteBuffer> mBufCache;

    public CacheByteBufferComponent() {
        mBufCache = new MultiplexCache();
    }


    @Override
    public MultiByteBuffer useBuffer() {
        MultiByteBuffer buffer = mBufCache.getCanUseData();
        if (buffer == null) {
            buffer = new MultiByteBuffer();
        }
        return buffer;
    }


    @Override
    public void reuseBuffer(MultiByteBuffer buffer) {
        if (!buffer.isIdle()) {
            throw new RuntimeException("## buf is busy, can not be reset !!!");
        }
        buffer.clear();
        mBufCache.resetData(buffer);
    }

    @Override
    public void release() {
        mBufCache.release();
    }
}
