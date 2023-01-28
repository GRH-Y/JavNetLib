package com.jav.net.component;

import com.jav.net.component.joggle.ICacheComponent;

import java.util.LinkedList;

/**
 * 缓存组件
 *
 * @author yyz
 */
public class CacheComponent implements ICacheComponent<Object> {

    protected final LinkedList<Object> mDataQueue = new LinkedList();

    @Override
    public boolean addLastData(Object data) {
        if (data == null) {
            return false;
        }
        synchronized (mDataQueue) {
            mDataQueue.addLast(data);
        }
        return true;
    }

    @Override
    public boolean addFirstData(Object data) {
        if (data == null) {
            return false;
        }
        synchronized (mDataQueue) {
            mDataQueue.addFirst(data);
        }
        return true;
    }


    @Override
    public Object pollFirstData() {
        synchronized (mDataQueue) {
            return mDataQueue.pollFirst();
        }
    }

    @Override
    public Object pollLastData() {
        synchronized (mDataQueue) {
            return mDataQueue.pollLast();
        }
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
            synchronized (mDataQueue) {
                for (Object data : mDataQueue) {
                    picker.clear(data);
                }
            }
        }
        synchronized (mDataQueue) {
            mDataQueue.clear();
        }
    }
}
