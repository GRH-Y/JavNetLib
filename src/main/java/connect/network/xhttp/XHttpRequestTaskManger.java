package connect.network.xhttp;

import connect.network.base.AbsNetFactory;
import connect.network.xhttp.entity.XRequest;
import util.MultiplexCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class XHttpRequestTaskManger {

    private MultiplexCache<XHttpRequestTask> multiplexCache;

    private Map<String, XHttpRequestTask> requestTaskMap;

    private XHttpRequestTaskManger() {
        requestTaskMap = new HashMap<>();
        multiplexCache = new MultiplexCache();
    }

    private static XHttpRequestTaskManger sTaskManger = null;

    public static synchronized XHttpRequestTaskManger getInstance() {
        if (sTaskManger == null) {
            synchronized (XHttpRequestTaskManger.class) {
                if (sTaskManger == null) {
                    sTaskManger = new XHttpRequestTaskManger();
                }
            }
        }
        return sTaskManger;
    }

    public static void destroy() {
        if (sTaskManger != null) {
            sTaskManger.requestTaskMap.clear();
            sTaskManger.multiplexCache.release();
            sTaskManger = null;
        }
    }

    public void pushTask(String key, XHttpRequestTask task) {
        requestTaskMap.put(key, task);
        multiplexCache.addData(task);
    }

    public XHttpRequestTask getTask(String key) {
        return requestTaskMap.get(key);
    }


    public Collection<XHttpRequestTask> getAllTask() {
        return requestTaskMap.values();
    }

    public XHttpRequestTask removerTask(String key) {
        XHttpRequestTask task = requestTaskMap.remove(key);
        multiplexCache.resetData(task);
        return task;
    }

    public XHttpRequestTask obtain(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
        XHttpRequestTask requestTask = multiplexCache.getRepeatData();
        if (requestTask == null) {
            requestTask = new XHttpRequestTask(netFactory, httpConfig, request);
        } else {
            requestTask.initTask(request);
        }
        return requestTask;
    }

}
