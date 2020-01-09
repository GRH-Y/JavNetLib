package connect.network.xhttp;

import connect.network.xhttp.entity.XHttpRequest;
import util.MultiplexCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class XHttpTaskManger {

    private MultiplexCache<XHttpRequestTask> multiplexCache;

    private Map<String, XHttpRequestTask> requestTaskMap;

    private XHttpTaskManger() {
        requestTaskMap = new HashMap<>();
        multiplexCache = new MultiplexCache();
    }

    private static class HelperHolder {
        public static final XHttpTaskManger helper = new XHttpTaskManger();
    }

    public static XHttpTaskManger getInstance() {
        return XHttpTaskManger.HelperHolder.helper;
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

    public XHttpRequestTask obtain(XHttpRequest request) {
        XHttpRequestTask requestTask = multiplexCache.getRepeatData();
        if (requestTask == null) {
            requestTask = new XHttpRequestTask(request);
        } else {
            requestTask.initTask(request);
        }
        return requestTask;
    }


    public void release() {
        requestTaskMap.clear();
        multiplexCache.release();
    }

}
