package connect.network.http;


import connect.network.base.RequestEntity;
import connect.network.http.joggle.*;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.IConsumerAttribute;
import task.executor.joggle.ILoopTaskExecutor;
import task.executor.joggle.ITaskContainer;
import util.LogDog;
import util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Http通信类
 * Created by No.9 on 7/7/2017.
 *
 * @author yyz
 */
public class JavHttpConnect {

    private static JavHttpConnect sConnect = null;

    private HttpCoreTask mCoreTask;
    private HttpTaskConfig mHttpTaskManage;


    /**
     * 默认的请求tag
     */
    public static final int DEFAULT_TASK_TAG = 0;


    protected JavHttpConnect() {
        mHttpTaskManage = new HttpTaskConfig();
        mCoreTask = new HttpCoreTask(mHttpTaskManage);
        ITaskContainer container = TaskExecutorPoolManager.getInstance().runTask(mCoreTask, mHttpTaskManage.getAttribute());
        mHttpTaskManage.setTaskContainer(container);
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

    public IHttpTaskConfig getHttpTaskConfig() {
        return mHttpTaskManage;
    }


    /**
     * 网络请求
     *
     * @param address              请求地址
     * @param type                 请求类型
     * @param sendData             发送的内容
     * @param scbMethodName        成功回调方法
     * @param resultType           接收数据转换的类型（可以传String的文件路径则把结果保存成文件，byte[]类型则不继续json解析，直接返回byte[]，其他类型则进行json解析并实例化实例）
     * @param ecbMethodName        失败回调方法
     * @param callBackTarget       回调接收类
     * @param viewTarget           可设置控件的类（一般是 activity fragment view windows）
     * @param isAutoSetDataForView 如果请求成功是否自己为控件设置值
     */
    private void submit(String address, ConnectType type, byte[] sendData, String scbMethodName, String ecbMethodName,
                        Object resultType, Object callBackTarget, Object viewTarget, boolean isAutoSetDataForView) {
        submit(DEFAULT_TASK_TAG, address, type, sendData, scbMethodName, ecbMethodName, resultType, callBackTarget, viewTarget, isAutoSetDataForView);
    }

    private void submit(int taskTag, String address, ConnectType type, byte[] sendData, String scbMethodName, String ecbMethodName, Object resultType, Object callBackTarget, Object viewTarget, boolean isAutoSetDataForView) {
        if (address != null && type != null) {
            if (ConnectType.GET == type || ConnectType.POST == type) {
                RequestEntity entity = new RequestEntity();
                entity.setTaskTag(taskTag);
                entity.setAddress(address);
                entity.setRequestMethod(type.getType());
                entity.setSendData(sendData);
                entity.setScbMethodName(scbMethodName);
                entity.setEcbMethodName(ecbMethodName);
                entity.setResultType(resultType);
                entity.setCallBackTarget(callBackTarget);
                entity.setViewTarget(viewTarget);
                entity.setAutoSetDataForView(isAutoSetDataForView);

                startTaskAndPushToCache(entity);
            }
        }
    }


    public void submitGet(RequestEntity entity) {
        if (entity == null) {
            return;
        }
        submit(entity);
    }


    public void submitPost(RequestEntity entity) {
        if (entity == null) {
            return;
        }
        entity.setRequestMethod(ConnectType.POST.getType());
        submit(entity);
    }


    private void submit(RequestEntity entity) {
        if (entity == null) {
            return;
        }
        startTaskAndPushToCache(entity);
    }

    private void startTaskAndPushToCache(RequestEntity entity) {
        IConsumerAttribute attribute = mHttpTaskManage.getAttribute();
        ILoopTaskExecutor executor = mHttpTaskManage.getExecutor();
        if (!executor.getAliveState()) {
            executor.startTask();
        } else if (executor.isIdleState() && executor.getAliveState()) {
            ITaskContainer container = TaskExecutorPoolManager.getInstance().runTask(mCoreTask, mHttpTaskManage.getAttribute());
            container.setAttribute(attribute);
            executor = container.getTaskExecutor();
            mHttpTaskManage.setTaskContainer(container);
        }
        attribute.pushToCache(entity);
        executor.resumeTask();
    }

    /**
     * 网络请求
     *
     * @param entity         发送数据实体
     * @param callBackTarget 请求回调接收者
     */
    public void submitEntity(IRequestEntity entity, Object callBackTarget) {
        submitEntity(entity, callBackTarget, null, false, DEFAULT_TASK_TAG);
    }

    public void submitEntity(IRequestEntity entity, Object callBackTarget, Object viewTarget) {
        submitEntity(entity, callBackTarget, viewTarget, false, DEFAULT_TASK_TAG);
    }

    public void submitEntity(IRequestEntity entity, Object callBackTarget, boolean isAutoSetDataForView) {
        submitEntity(entity, callBackTarget, null, isAutoSetDataForView, DEFAULT_TASK_TAG);
    }

    public void submitEntity(IRequestEntity entity, Object callBackTarget, int taskTag) {
        submitEntity(entity, callBackTarget, null, false, taskTag);
    }

    /**
     * 网络请求
     *
     * @param entity               请求体ARequest
     * @param callBackTarget       回调接收类
     * @param viewTarget           可设置控件的类（一般是 activity fragment view windows）
     * @param isAutoSetDataForView 是否自动为控件设值
     */
    public void submitEntity(IRequestEntity entity, Object callBackTarget, Object viewTarget, boolean isAutoSetDataForView, int taskTag) {
        Class clx = entity.getClass();
        ARequest request = (ARequest) clx.getAnnotation(ARequest.class);
        int atnTaskTag = taskTag != DEFAULT_TASK_TAG ? taskTag : request.taskTag();
        Class requestMethod = request.requestMethod();
        Object resultType = request.savePath() == null || request.savePath().length() == 0 ? request.resultType() : request.savePath();
        String address = mHttpTaskManage.getBaseUrl() == null ? request.url() : mHttpTaskManage.getBaseUrl() + request.url();

        RequestEntity netTaskEntity = new RequestEntity();
        netTaskEntity.setTaskTag(atnTaskTag);
        netTaskEntity.setScbMethodName(request.successMethod());
        netTaskEntity.setEcbMethodName(request.errorMethod());
        netTaskEntity.setCallBackTarget(callBackTarget);
        netTaskEntity.setViewTarget(viewTarget);
        netTaskEntity.setResultType(resultType);
        netTaskEntity.setAutoSetDataForView(isAutoSetDataForView);
        netTaskEntity.setRequestMethod(requestMethod.getSimpleName());

        if (requestMethod == POST.class) {
            byte[] postData = entity.postData();
            if (postData != null) {
                LogDog.d("==>JavHttpConnect post submitEntity = " + new String(postData));
            }
            netTaskEntity.setAddress(address);
            netTaskEntity.setSendData(postData);
            submitPost(netTaskEntity);
        } else if (requestMethod == GET.class) {
            StringBuilder builder = new StringBuilder();
            builder.append(address);

//            Map<String, String> header = new HashMap<>(8);
//            entity.getConnect(header);
//            for (String key : header.keySet()) {
//                builder.append(key);
//                builder.append("=");
//                builder.append(header.get(key));
//                builder.append("&");
//            }
            String ret = entityToStr(clx, entity);
            if (!StringUtils.isEmpty(ret)) {
                builder.append("?");
                builder.append(ret);
            }
            netTaskEntity.setAddress(builder.toString());
            submitGet(netTaskEntity);
        }
    }

    /**
     * get请求就是把实体内容封装成请求地址
     */
    private String entityToStr(Class clx, IRequestEntity entity) {
        StringBuilder builder = new StringBuilder();
        Field[] files = clx.getDeclaredFields();
        for (Field field : files) {
            String name = field.getName();
            if ("shadow$_monitor_".equals(name)) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object object = field.get(entity);
                builder.append(name);
                builder.append("=");
                builder.append(object);
                builder.append("&");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Class supperClx = clx.getSuperclass();
        if (supperClx != IRequestEntity.class && supperClx != Object.class) {
            String ret = entityToStr(supperClx, entity);
            if (!StringUtils.isEmpty(ret)) {
                builder.append(ret);
            }
        }
        return builder.toString();
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

    public void cancelSubmit(IRequestEntity entity) {
        if (entity != null) {
            Queue<RequestEntity> queue = mHttpTaskManage.getAttribute().getCache();
            Class cancelClx = entity.getClass();
            ARequest cancelRequest = (ARequest) cancelClx.getAnnotation(ARequest.class);
            List<RequestEntity> record = new ArrayList<>();
            for (RequestEntity requestEntity : queue) {
                if (cancelRequest.url().equals(requestEntity.getAddress())) {
                    record.add(requestEntity);
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
