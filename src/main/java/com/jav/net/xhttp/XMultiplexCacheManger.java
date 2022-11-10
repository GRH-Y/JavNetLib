package com.jav.net.xhttp;

import com.jav.common.util.MultiplexCache;
import com.jav.net.aio.AioClientTask;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.nio.NioClientTask;
import com.jav.net.xhttp.config.XHttpConfig;
import com.jav.net.xhttp.entity.XRequest;

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


    public XNioHttpTask obtainNioTask(INetTaskComponent<NioClientTask> taskFactory, XHttpConfig httpConfig, XRequest request) {
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

    public XAioHttpTask obtainAioTask(INetTaskComponent<AioClientTask> taskFactory, XHttpConfig httpConfig, XRequest request) {
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
