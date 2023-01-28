package com.jav.net.component;

import com.jav.net.component.joggle.ICacheComponent;
import com.jav.net.entity.MultiByteBuffer;

public class DefaultCacheComponentPicker implements ICacheComponent.IClearPolicy {

    @Override
    public void clear(Object data) {
        if (data instanceof MultiByteBuffer) {
            MultiByteBuffer buffer = (MultiByteBuffer) data;
            buffer.release();
        }
    }
}
