package com.currency.net.xhttp;

import com.currency.net.aio.AioClientTask;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.nio.NioClientTask;
import com.currency.net.xhttp.config.XHttpConfig;
import com.currency.net.xhttp.entity.XRequest;
import util.MultiplexCache;

public class XMultiplexCacheManger {

    private final MultiplexCache<XNioHttpTask> mNioTaskMultiplexCache;
    private final MultiplexCache<XAioHttpTask> mAioTaskMultiplexCache;
    private volatile boolean mIsRelease = false;


    private XMultiplexCacheManger() {
        mNioTaskMultiplexCache = new MultiplexCache<>();
        mAioTaskMultiplexCache = new MultiplexCache<>();
    }

    private static final class InnerClass {
        public static final XMultiplexCacheManger sManager = new XMultiplexCacheManger();
    }

    public static XMultiplexCacheManger getInstance() {
        return InnerClass.sManager;
    }

    public void destroy() {
        if (mIsRelease) {
            return;
        }
        mIsRelease = true;
        mNioTaskMultiplexCache.release();
        mAioTaskMultiplexCache.release();
    }

    public void lose(XAioHttpTask task) {
        if (task != null && !mIsRelease) {
            mAioTaskMultiplexCache.resetData(task);
        }
    }

    public void lose(XNioHttpTask task) {
        if (task != null && !mIsRelease) {
            mNioTaskMultiplexCache.resetData(task);
        }
    }


    public XNioHttpTask obtainNioTask(INetTaskContainer<NioClientTask> taskFactory, XHttpConfig httpConfig, XRequest request) {
        if (mIsRelease) {
            return null;
        }
        XNioHttpTask requestTask = mNioTaskMultiplexCache.getCanUseData();
        if (requestTask == null) {
            requestTask = new XNioHttpTask(taskFactory, httpConfig, request);
        } else {
            requestTask.initTask(request);
        }
        return requestTask;
    }

    public XAioHttpTask obtainAioTask(INetTaskContainer<AioClientTask> taskFactory, XHttpConfig httpConfig, XRequest request) {
        if (mIsRelease) {
            return null;
        }
        XAioHttpTask requestTask = mAioTaskMultiplexCache.getCanUseData();
        if (requestTask == null) {
            requestTask = new XAioHttpTask(taskFactory, httpConfig, request);
        } else {
            requestTask.initTask(request);
        }
        return requestTask;
    }

}
