package com.jav.net.component;

import com.jav.net.component.joggle.IBufferComponent;
import com.jav.net.entity.MultiByteBuffer;

/**
 * ByteBuffer
 *
 * @author yyz
 */
public class DefaultByteBufferComponent implements IBufferComponent<MultiByteBuffer> {

    @Override
    public MultiByteBuffer useBuffer() {
        return new MultiByteBuffer();
    }


    @Override
    public void reuseBuffer(MultiByteBuffer buffer) {
        buffer.release();
    }

    @Override
    public void release() {
    }
}
