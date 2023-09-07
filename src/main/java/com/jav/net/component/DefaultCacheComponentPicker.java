package com.jav.net.component;

import com.jav.net.base.MultiBuffer;
import com.jav.net.component.joggle.ICacheComponent;

public class DefaultCacheComponentPicker implements ICacheComponent.IClearPolicy<MultiBuffer> {

    @Override
    public void clear(MultiBuffer data) {
        data.release();
    }
}
