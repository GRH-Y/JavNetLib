package connect.network.http;


import connect.network.base.RequestMode;
import connect.network.http.joggle.ARequest;
import connect.network.http.joggle.IHttpTaskConfig;
import connect.network.http.joggle.IRequestEntity;
import connect.network.http.tool.JavConvertResult;
import connect.network.http.tool.JavSessionNotify;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;
import util.StringEnvoy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Http通信类
 * Created by No.9 on 7/7/2017.
 *
 * @author yyz
 */
public class JavHttpConnect {

    private static JavHttpConnect sConnect = null;

    protected HttpCoreTask mCoreTask;
    protected HttpTaskConfig mHttpTaskManage;

    protected JavHttpConnect() {
        init();
    }

    public static synchronized JavHttpConnect getInstance() {
        if (sConnect == null) {
            synchronized (JavHttpConnect.class) {
                if (sConnect == null) {
                    sConnect = new JavHttpConnect();
                }
            }
        }
        return sConnect;
    }

    protected void init() {
        mHttpTaskManage = new HttpTaskConfig();
        JavSessionNotify sessionNotify = initJavSessionCallBack();
        if (sessionNotify == null) {
            sessionNotify = new JavSessionNotify();
        }
        mHttpTaskManage.setSessionNotify(sessionNotify);
        JavConvertResult convertResult = initJavConvertResult();
        if (convertResult == null) {
            convertResult = new JavConvertResult();
        }
        mHttpTaskManage.setConvertResult(convertResult);
        mHttpTaskManage.setTimeout(8000);
        mCoreTask = new HttpCoreTask(mHttpTaskManage);
        ITaskContainer container = TaskExecutorPoolManager.getInstance().runTask(mCoreTask, mHttpTaskManage.getAttribute());
        mHttpTaskManage.setTaskContainer(container);
    }

    protected JavSessionNotify initJavSessionCallBack() {
        return null;
    }

    protected JavConvertResult initJavConvertResult() {
        return null;
    }

    public IHttpTaskConfig getHttpTaskConfig() {
        return mHttpTaskManage;
    }


    public void submitEntity(RequestEntity requestEntity) {
        if (requestEntity != null) {
            requestEntity.setException(null);
            startTaskAndPushToCache(requestEntity);
        }
    }


    /**
     * 网络请求
     *
     * @param entity         发送数据实体
     * @param callBackTarget 请求回调接收者
     */
    public void submitEntity(IRequestEntity entity, Object callBackTarget) {
        submitEntity(RequestEntity.DEFAULT_TASK_TAG, entity, callBackTarget);
    }

    /**
     * 网络请求
     *
     * @param requestEntity  请求体ARequest
     * @param callBackTarget 回调接收类
     */
    public void submitEntity(int taskTag, IRequestEntity requestEntity, Object callBackTarget) {
        if (requestEntity == null) {
            throw new NullPointerException("JavHttpConnect submitEntity requestEntity is null ");
        }
        Class clx = requestEntity.getClass();
        ARequest request = (ARequest) clx.getAnnotation(ARequest.class);
        if (request == null) {
            throw new IllegalArgumentException("The entity has no annotations ARequest !!! ");
        }
        int atnTaskTag = taskTag != RequestEntity.DEFAULT_TASK_TAG ? taskTag : request.taskTag();
        RequestMode requestMode = request.requestMode();
        String address = request.url().trim();
        if (StringEnvoy.isEmpty(address)) {
            throw new NullPointerException("JavHttpConnect submitEntity request.url() is null ");
        }

        RequestEntity netTaskEntity = new RequestEntity();
        netTaskEntity.setTaskTag(atnTaskTag);
        netTaskEntity.setDisableBaseUrl(request.disableBaseUrl());
        netTaskEntity.setSuccessMethod(request.successMethod());
        netTaskEntity.setErrorMethod(request.errorMethod());
        netTaskEntity.setProcessMethod(request.processMethod());
        netTaskEntity.setCallBackTarget(callBackTarget);
        netTaskEntity.setResultType(request.resultType());
        netTaskEntity.setRequestMode(requestMode);
        netTaskEntity.setIndependentTask(request.isIndependentTask());

        Map<Object, Object> property = requestEntity.getRequestProperty();
        netTaskEntity.setRequestProperty(property);
        byte[] data = requestEntity.getSendData();

        if (requestMode == RequestMode.POST) {
            netTaskEntity.setAddress(address);
            netTaskEntity.setSendData(data);
            startTaskAndPushToCache(netTaskEntity);
        } else if (requestMode == RequestMode.GET) {
            StringBuilder builder = new StringBuilder();
            builder.append(address);
            if (data != null) {
                builder.append("?");
                builder.append(new String(data));
            }
            String url = builder.toString();
            netTaskEntity.setAddress(url);
            startTaskAndPushToCache(netTaskEntity);
        }
    }


    private void startTaskAndPushToCache(RequestEntity entity) {
        IConsumerAttribute attribute = mHttpTaskManage.getAttribute();
        attribute.pushToCache(entity);
        if (entity.isIndependentTask()) {
            //一次性任务
            ITaskContainer container = TaskExecutorPoolManager.getInstance().createLoopTask(mCoreTask, mHttpTaskManage.getAttribute());
            container.getTaskExecutor().blockStartTask();
            container.getTaskExecutor().stopTask();
        } else {
            ILoopTaskExecutor executor = mHttpTaskManage.getExecutor();
            if (!executor.isAliveState()) {
                executor.startTask();
            } else if (executor.isIdleState() && executor.isAliveState()) {
                ITaskContainer container = TaskExecutorPoolManager.getInstance().runTask(mCoreTask, attribute);
                executor = container.getTaskExecutor();
                mHttpTaskManage.setTaskContainer(container);
            }
            executor.resumeTask();
        }
    }


    //    ---------------------------submitEntity----------------------------------

    public void cancelSubmit(String url) {
        if (url != null && url.length() > 0) {
            Queue<RequestEntity> queue = mHttpTaskManage.getAttribute().getCache();
            List<RequestEntity> record = new ArrayList<>();
            for (RequestEntity entity : queue) {
                if (url.equals(entity.getAddress())) {
                    record.add(entity);
                }
            }
            if (!record.isEmpty()) {
                queue.removeAll(record);
            }
        }
    }

    public void cancelSubmit(Object requestEntity) {
        if (requestEntity != null) {
            Queue<RequestEntity> queue = mHttpTaskManage.getAttribute().getCache();
            Class cancelClx = requestEntity.getClass();
            ARequest cancelRequest = (ARequest) cancelClx.getAnnotation(ARequest.class);
            List<RequestEntity> record = new ArrayList<>();
            for (RequestEntity entity : queue) {
                if (cancelRequest.url().equals(entity.getAddress())) {
                    record.add(entity);
                }
            }
            if (!record.isEmpty()) {
                queue.removeAll(record);
            }
        }
    }

    public void cancelSubmit(int taskTag) {
        Queue<RequestEntity> queue = mHttpTaskManage.getAttribute().getCache();
        List<RequestEntity> record = new ArrayList<>();
        for (RequestEntity entity : queue) {
            if (entity.getTaskTag() == taskTag) {
                record.add(entity);
            }
        }
        if (!record.isEmpty()) {
            queue.removeAll(record);
        }
    }

    public void cancelAllSubmit() {
        mHttpTaskManage.getAttribute().clearCacheData();
    }

    /**
     * 释放资源
     */
    public synchronized void recycle() {
        if (mHttpTaskManage != null) {
            mHttpTaskManage.recycle();
        }
        sConnect = null;
    }

}
