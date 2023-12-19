package com.jav.net.component;

import com.jav.net.component.joggle.ICacheComponent;

import java.util.LinkedList;

/**
 * 缓存组件
 *
 * @author yyz
 */
public class CacheComponent<T> implements ICacheComponent<T> {

    protected final LinkedList<T> mDataQueue = new LinkedList();

    @Override
    public boolean addLastData(T data) {
        if (data == null) {
            return false;
        }
        synchronized (mDataQueue) {
            mDataQueue.addLast(data);
        }
        return true;
    }

    @Override
    public boolean addFirstData(T data) {
        if (data == null) {
            return false;
        }
        synchronized (mDataQueue) {
            mDataQueue.addFirst(data);
        }
        return true;
    }


    @Override
    public T pollFirstData() {
        synchronized (mDataQueue) {
            return mDataQueue.pollFirst();
        }
    }

    @Override
    public T pollLastData() {
        synchronized (mDataQueue) {
            return mDataQueue.pollLast();
        }
    }

    @Override
    public int size() {
        return mDataQueue.size();
    }


    @Override
    public void clearCache(IClearPolicy<T> picker) {
        if (mDataQueue.isEmpty()) {
            return;
        }
        if (picker != null) {
            synchronized (mDataQueue) {
                for (T data : mDataQueue) {
                    picker.clear(data);
                }
            }
        }
        synchronized (mDataQueue) {
            mDataQueue.clear();
        }
    }
}
