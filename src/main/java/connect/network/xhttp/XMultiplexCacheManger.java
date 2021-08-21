package connect.network.xhttp;

import connect.network.base.AbsNetFactory;
import connect.network.xhttp.config.XHttpConfig;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.utils.MultiLevelBuf;
import util.MultiplexCache;

public class XMultiplexCacheManger {

    private MultiplexCache<XNioHttpTask> mNioTaskMultiplexCache;
    private MultiplexCache<XAioHttpTask> mAioTaskMultiplexCache;
    private MultiplexCache<MultiLevelBuf> mBufMultiplexCache;
    private volatile boolean mIsRelease = false;


    private XMultiplexCacheManger() {
        mNioTaskMultiplexCache = new MultiplexCache<>();
        mAioTaskMultiplexCache = new MultiplexCache<>();
        mBufMultiplexCache = new MultiplexCache<>();
    }

    private static XMultiplexCacheManger sTaskManger = null;

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

    public void lose(MultiLevelBuf buf) {
        if (buf != null && !mIsRelease) {
            buf.clear();
            mBufMultiplexCache.resetData(buf);
        }
    }

    public MultiLevelBuf obtainBuf() {
        if (mIsRelease) {
            return null;
        }
        MultiLevelBuf buf = mBufMultiplexCache.getCanUseData();
        if (buf == null) {
            buf = new MultiLevelBuf();
        }
        return buf;
    }


    public XNioHttpTask obtainNioTask(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
        if (mIsRelease) {
            return null;
        }
        XNioHttpTask requestTask = mNioTaskMultiplexCache.getCanUseData();
        if (requestTask == null) {
            requestTask = new XNioHttpTask(netFactory, httpConfig, request);
        } else {
            requestTask.initTask(request);
        }
        return requestTask;
    }

    public XAioHttpTask obtainAioTask(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
        if (mIsRelease) {
            return null;
        }
        XAioHttpTask requestTask = mAioTaskMultiplexCache.getCanUseData();
        if (requestTask == null) {
            requestTask = new XAioHttpTask(netFactory, httpConfig, request);
        } else {
            requestTask.initTask(request);
        }
        return requestTask;
    }

}
