package connect.network.xhttp;

import connect.network.base.AbsNetFactory;
import connect.network.xhttp.config.XHttpConfig;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.utils.MultiLevelBuf;
import util.MultiplexCache;

public class XMultiplexCacheManger {

    private MultiplexCache<XNioHttpTask> nioTaskMultiplexCache;
    private MultiplexCache<XAioHttpTask> aioTaskMultiplexCache;
    private MultiplexCache<MultiLevelBuf> bufMultiplexCache;
    private volatile boolean isRelease = false;


    private XMultiplexCacheManger() {
        nioTaskMultiplexCache = new MultiplexCache<>();
        aioTaskMultiplexCache = new MultiplexCache<>();
        bufMultiplexCache = new MultiplexCache<>();
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
            sTaskManger.isRelease = true;
            sTaskManger.nioTaskMultiplexCache.release();
            sTaskManger.aioTaskMultiplexCache.release();
            sTaskManger.bufMultiplexCache.release();
        }
    }

    public void lose(XAioHttpTask task) {
        if (task != null && !isRelease) {
            aioTaskMultiplexCache.resetData(task);
        }
    }

    public void lose(XNioHttpTask task) {
        if (task != null && !isRelease) {
            nioTaskMultiplexCache.resetData(task);
        }
    }

    public void lose(MultiLevelBuf buf) {
        if (buf != null && !isRelease) {
            buf.clear();
            bufMultiplexCache.resetData(buf);
        }
    }

    public MultiLevelBuf obtainBuf() {
        if (isRelease) {
            return null;
        }
        MultiLevelBuf buf = bufMultiplexCache.getCanUseData();
        if (buf == null) {
            buf = new MultiLevelBuf();
        }
        return buf;
    }


    public XNioHttpTask obtainNioTask(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
        if (isRelease) {
            return null;
        }
        XNioHttpTask requestTask = nioTaskMultiplexCache.getCanUseData();
        if (requestTask == null) {
            requestTask = new XNioHttpTask(netFactory, httpConfig, request);
        } else {
            requestTask.initTask(request);
        }
        return requestTask;
    }

    public XAioHttpTask obtainAioTask(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
        if (isRelease) {
            return null;
        }
        XAioHttpTask requestTask = aioTaskMultiplexCache.getCanUseData();
        if (requestTask == null) {
            requestTask = new XAioHttpTask(netFactory, httpConfig, request);
        } else {
            requestTask.initTask(request);
        }
        return requestTask;
    }

}
