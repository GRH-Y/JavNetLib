package com.currency.net.xhttp;

import com.currency.net.aio.AioClientTask;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.nio.NioClientTask;
import com.currency.net.xhttp.config.XHttpConfig;
import com.currency.net.xhttp.entity.XRequest;
import com.currency.net.xhttp.utils.ReuseDirectBuf;
import util.MultiplexCache;

public class XMultiplexCacheManger {

    private final MultiplexCache<XNioHttpTask> mNioTaskMultiplexCache;
    private final MultiplexCache<XAioHttpTask> mAioTaskMultiplexCache;
    private final MultiplexCache<ReuseDirectBuf> mBufMultiplexCache;
    private volatile boolean mIsRelease = false;


    private XMultiplexCacheManger() {
        mNioTaskMultiplexCache = new MultiplexCache<>();
        mAioTaskMultiplexCache = new MultiplexCache<>();
        mBufMultiplexCache = new MultiplexCache<>();
    }

    private volatile static XMultiplexCacheManger sTaskManger = null;

    public static synchronized XMultiplexCacheManger getInstance() {
        if (sTaskManger == null) {
            synchronized (XMultiplexCacheManger.class) {
                if (sTaskManger == null) {
                    sTaskManger = new XMultiplexCacheManger();
                }
            }
        }
        return sTaskManger;
    }

    public static void destroy() {
        if (sTaskManger != null) {
            sTaskManger.mIsRelease = true;
            sTaskManger.mNioTaskMultiplexCache.release();
            sTaskManger.mAioTaskMultiplexCache.release();
            sTaskManger.mBufMultiplexCache.release();
        }
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

    public void lose(ReuseDirectBuf buf) {
        if (buf != null && !mIsRelease) {
            buf.clear();
            mBufMultiplexCache.resetData(buf);
        }
    }

    public ReuseDirectBuf obtainBuf() {
        if (mIsRelease) {
            return null;
        }
        ReuseDirectBuf buf = mBufMultiplexCache.getCanUseData();
        if (buf == null) {
            buf = new ReuseDirectBuf();
        }
        return buf;
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
