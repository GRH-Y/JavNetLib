package com.jav.net.component;

import com.jav.net.component.joggle.ICacheComponent;
import com.jav.net.entity.MultiByteBuffer;

import java.nio.ByteBuffer;

public class DefaultClearCallBack implements ICacheComponent.IClearCallBack {

    @Override
    public void clear(Object data) {
        if (data instanceof MultiByteBuffer) {
            MultiByteBuffer buffer = (MultiByteBuffer) data;
            ByteBuffer[] tmpBuf = buffer.getTmpBuf();
            if (tmpBuf != null) {
                buffer.setBackBuf(tmpBuf);
            }
            buffer.release();
        }
    }
}
