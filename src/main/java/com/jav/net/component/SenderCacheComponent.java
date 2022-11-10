package com.jav.net.component;

import com.jav.net.component.joggle.ICacheComponent;

import java.util.LinkedList;

/**
 * 发送者缓存组件
 *
 * @author yyz
 */
public class SenderCacheComponent implements ICacheComponent<Object> {

    protected final LinkedList<Object> mDataQueue = new LinkedList();

    @Override
    public boolean addLastData(Object data) {
        if (data == null) {
            return false;
        }
        mDataQueue.addLast(data);
        return true;
    }

    @Override
    public boolean addFirstData(Object data) {
        if (data == null) {
            return false;
        }
        mDataQueue.addFirst(data);
        return true;
    }


    @Override
    public Object pollFirstData() {
        return mDataQueue.pollFirst();
    }

    @Override
    public Object pollLastData() {
        return mDataQueue.pollLast();
    }

    @Override
    public int size() {
        return mDataQueue.size();
    }

    @Override
    public void clearCache(IClearPolicy picker) {
        if (mDataQueue.isEmpty()) {
            return;
        }
        if (picker != null) {
            for (Object data : mDataQueue) {
                picker.clear(data);
            }
        }
        mDataQueue.clear();
    }
}
