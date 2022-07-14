package com.jav.net.component.joggle;

public interface ICacheComponent<T> {

    interface IClearCallBack<T> {
        void clear(T data);
    }

    boolean addLastData(T data);

    boolean addFirstData(T data);

    T pollFirstData();

    T pollLastData();

    int size();

    void clearCache(IClearCallBack callBack);
}
