package connect.network.xhttp;

import java.util.HashMap;
import java.util.Map;

public class XHttpTaskManger {
    private Map<String, XHttpRequestTask> requestTaskMap;

    private XHttpTaskManger() {
        requestTaskMap = new HashMap<>();
    }

    private static class HelperHolder {
        public static final XHttpTaskManger helper = new XHttpTaskManger();
    }

    public static XHttpTaskManger getInstance() {
        return XHttpTaskManger.HelperHolder.helper;
    }

    public void pushTask(String key, XHttpRequestTask task) {
        requestTaskMap.put(key, task);
    }

    public XHttpRequestTask removerTask(String key) {
        return requestTaskMap.remove(key);
    }

}
