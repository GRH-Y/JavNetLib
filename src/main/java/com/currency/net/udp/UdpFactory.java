package com.currency.net.udp;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.ISSLFactory;

public class UdpFactory extends AbsNetFactory<UdpTask> {

    private volatile static UdpFactory mFactory;

    public UdpFactory() {
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BioEngine(getFactoryIntent());
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new UdpWork(getFactoryIntent());
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return null;
    }

    public synchronized static UdpFactory getFactory() {
        if (mFactory == null) {
            synchronized (UdpFactory.class) {
                if (mFactory == null) {
                    mFactory = new UdpFactory();
                }
            }
        }
        return mFactory;
    }

    public static void destroy() {
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }
}
