package com.jav.net.component;

import com.jav.common.util.MultiplexCache;
import com.jav.net.component.joggle.IBufferComponent;
import com.jav.net.entity.MultiByteBuffer;

/**
 * ByteBuffer
 *
 * @author yyz
 */
public class ByteBufferComponent implements IBufferComponent {

    private final MultiplexCache<MultiByteBuffer> mBufCache;

    public ByteBufferComponent() {
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
    public <T> void reuseBuffer(T buffer) {
        MultiByteBuffer byteBuffer = (MultiByteBuffer) buffer;
        if (!byteBuffer.isIdle()) {
            throw new RuntimeException("## buf is busy, can not be reset !!!");
        }
        mBufCache.resetData(byteBuffer);
    }
}
