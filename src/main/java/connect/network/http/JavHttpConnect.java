package connect.network.http;


import connect.json.JsonUtils;
import connect.network.base.Interface.ISessionCallBack;
import connect.network.base.JavSessionCallBack;
import connect.network.base.NetTaskEntity;
import connect.network.http.Interface.ARequest;
import connect.network.http.Interface.GET;
import connect.network.http.Interface.INetEntity;
import connect.network.http.Interface.POST;
import task.executor.BaseConsumerTask;
import task.executor.ConsumerTaskExecutor;
import task.executor.TaskContainer;
import task.utils.IoUtils;
import task.utils.Logcat;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Http通信类
 * Created by No.9 on 7/7/2017.
 *
 * @author yyz
 */
public class JavHttpConnect {

    private static ISessionCallBack callBack = null;
    private static TaskContainer container = null;
    private static JavHttpConnect connect = null;
    private static String baseUrl = null;

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static void setBaseUrl(String baseUrl) {
        JavHttpConnect.baseUrl = baseUrl;
    }

    /**
     * 请求类型
     */
    public enum ConnectType {
        POST("POST"), GET("GET");

        private String type;

        ConnectType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    private JavHttpConnect(ISessionCallBack callBack) {
        JavHttpConnect.callBack = callBack;
        init();
    }

    private JavHttpConnect() {
        callBack = new JavSessionCallBack();
        init();
    }

    private void init() {
        CoreTask coreTask = new CoreTask();
        container = new TaskContainer(coreTask);
    }

    public static synchronized JavHttpConnect getInstance(ISessionCallBack callBack) {
        if (connect == null) {
            synchronized (JavHttpConnect.class) {
                if (connect == null) {
                    connect = new JavHttpConnect(callBack);
                }
            }
        } else {
            if (JavHttpConnect.callBack != callBack) {
                JavHttpConnect.callBack = callBack;
            }
        }
        return connect;
    }

    public static synchronized JavHttpConnect getInstance(Object target) {
        if (connect == null) {
            synchronized (JavHttpConnect.class) {
                if (connect == null) {
                    connect = new JavHttpConnect();
                }
            }
        }
        JavHttpConnect.callBack.setCallBackTarget(target);
        return connect;
    }

    public static synchronized JavHttpConnect getInstance() {
        if (connect == null) {
            synchronized (JavHttpConnect.class) {
                if (connect == null) {
                    connect = new JavHttpConnect();
                }
            }
        }
        return connect;
    }

    public static ISessionCallBack getCallBack() {
        return callBack;
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
    private void submit(String address, ConnectType type, byte[] sendData, String scbMethodName, String ecbMethodName, Object resultType, Object callBackTarget, Object viewTarget, boolean isAutoSetDataForView) {
        if (address != null && type != null) {
            if (ConnectType.GET == type || ConnectType.POST == type) {
                ConsumerTaskExecutor consumerTaskExecutor = container.getTaskExecutor();
                consumerTaskExecutor.startTask();
                NetTaskEntity entity = new NetTaskEntity();
                entity.setAddress(address);
                entity.setRequestMethod(type.getType());
                entity.setSendData(sendData);
                entity.setScbMethodName(scbMethodName);
                entity.setEcbMethodName(ecbMethodName);
                entity.setResultType(resultType);
                entity.setCallBackTarget(callBackTarget);
                entity.setViewTarget(viewTarget);
                entity.setAutoSetDataForView(isAutoSetDataForView);
                consumerTaskExecutor.pushToCache(entity);
            }
        }
    }

    private void submitGet(NetTaskEntity entity) {
        if (entity == null) {
            return;
        }
        submit(entity);
    }


    private void submitPost(NetTaskEntity entity) {
        if (entity == null) {
            return;
        }
        entity.setRequestMethod(ConnectType.POST.getType());
        submit(entity);
    }


    private void submit(NetTaskEntity entity) {
        if (entity == null) {
            return;
        }
        ConsumerTaskExecutor consumerTaskExecutor = container.getTaskExecutor();
        consumerTaskExecutor.startTask();
        consumerTaskExecutor.pushToCache(entity);
    }

    /**
     * 网络请求
     *
     * @param entity         发送数据实体
     * @param callBackTarget 请求回调接收者
     */
    public void submitEntity(INetEntity entity, Object callBackTarget) {
        submitEntity(entity, callBackTarget, null, false);
    }

    public void submitEntity(INetEntity entity, Object callBackTarget, Object viewTarget) {
        submitEntity(entity, callBackTarget, viewTarget, false);
    }

    public void submitEntity(INetEntity entity, Object callBackTarget, boolean isAutoSetDataForView) {
        submitEntity(entity, callBackTarget, null, isAutoSetDataForView);
    }

    /**
     * 网络请求
     *
     * @param entity               请求体ARequest
     * @param callBackTarget       回调接收类
     * @param viewTarget           可设置控件的类（一般是 activity fragment view windows）
     * @param isAutoSetDataForView 是否自动为控件设值
     */
    public void submitEntity(INetEntity entity, Object callBackTarget, Object viewTarget, boolean isAutoSetDataForView) {
        Class clx = entity.getClass();
        ARequest request = (ARequest) clx.getAnnotation(ARequest.class);
        Class requestType = request.requestType();
        Object resultType = request.savePath() == null || request.savePath().length() == 0 ? request.resultType() : request.savePath();
        String address = baseUrl == null ? request.url() : baseUrl + request.url();

        NetTaskEntity netTaskEntity = new NetTaskEntity();
        netTaskEntity.setAddress(address);
        netTaskEntity.setScbMethodName(request.successMethod());
        netTaskEntity.setEcbMethodName(request.errorMethod());
        netTaskEntity.setCallBackTarget(callBackTarget);
        netTaskEntity.setViewTarget(viewTarget);
        netTaskEntity.setResultType(resultType);
        netTaskEntity.setAutoSetDataForView(isAutoSetDataForView);

        if (requestType == POST.class) {
            Logcat.d("==>JavHttpConnect post submitEntity = " + new String(entity.postData()));
            netTaskEntity.setAddress(address);
            netTaskEntity.setSendData(entity.postData());
            submitPost(netTaskEntity);
        } else if (requestType == GET.class) {
            StringBuilder builder = new StringBuilder();
            builder.append(address);
            builder.append("?");

//            Map<String, String> header = new HashMap<>(8);
//            entity.getConnect(header);
//            for (String key : header.keySet()) {
//                builder.append(key);
//                builder.append("=");
//                builder.append(header.get(key));
//                builder.append("&");
//            }
            entityToStr(clx, builder, entity);
            netTaskEntity.setAddress(builder.toString());
            submitGet(netTaskEntity);
        }
    }

    /**
     * get请求就是把实体内容封装成请求地址
     */
    private void entityToStr(Class clx, StringBuilder builder, INetEntity entity) {
        Field[] files = clx.getDeclaredFields();
        for (Field field : files) {
            field.setAccessible(true);
            try {
                Object object = field.get(entity);
                builder.append(field.getName());
                builder.append("=");
                builder.append(object);
                builder.append("&");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Class supperClx = clx.getSuperclass();
        if (supperClx != INetEntity.class && supperClx != Object.class) {
            entityToStr(supperClx, builder, entity);
        }
    }

    //    ---------------------------submitEntity----------------------------------

    /**
     * 释放资源
     */
    public synchronized static void recycle() {
        if (container != null) {
            ConsumerTaskExecutor consumerTaskExecutor = container.getTaskExecutor();
            consumerTaskExecutor.stopTask();
        }
        if (connect != null && connect.callBack != null) {
            connect.callBack.recycle();
            connect = null;
        }
        container = null;
    }


    private class CoreTask extends BaseConsumerTask<NetTaskEntity> {

        private HttpURLConnection init(NetTaskEntity task) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(task.getAddress());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestMethod(task.getRequestMethod());

                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setRequestProperty("Accept-Charset", "utf-8");
                connection.setRequestProperty("Accept-Encoding", "gzip");
//                    connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
                connection.setRequestProperty("User-Agent", "HttpURLConnection");
//                    connection.setRequestProperty("Accept", "application/json");
                //此处为暴力方法设置接受所有类型，以此来防范返回415;
                connection.setRequestProperty("Accept", "*/*");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return connection;
        }

        @Override
        public void onCreateData() {
            ConsumerTaskExecutor consumerTaskExecutor = container.getTaskExecutor();
            if (consumerTaskExecutor.getCacheDataSize() == 0) {
                consumerTaskExecutor.waitTask(30000);
                if (consumerTaskExecutor.getCacheDataSize() == 0) {
                    consumerTaskExecutor.stopTask();
                }
            }
        }

        @Override
        protected void onProcess() {
            ConsumerTaskExecutor<NetTaskEntity> consumerTaskExecutor = container.getTaskExecutor();
            onProcessData(consumerTaskExecutor.popCacheData());
        }


        /**
         * 发送时转换编码，解决编码问题
         * String sendData = URLEncoder.encode(String.valueOf(enCodeData.sendData), "utf-8");
         * OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
         * writer.write(sendData);
         * writer.close();
         */


        private void onProcessData(NetTaskEntity data) {
            HttpURLConnection connection = init(data);
            if (connection == null) {
                ConsumerTaskExecutor consumerTaskExecutor = container.getTaskExecutor();
                if (callBack != null && consumerTaskExecutor.getLoopState()) {
                    data.setResultData(null);
                    callBack.notifyErrorMessage(data);
                }
                return;
            }
            try {
                if (data.getSendData() != null) {
                    OutputStream os = connection.getOutputStream();
                    os.write(data.getSendData());
                    os.flush();
                }

                int code = connection.getResponseCode();
//                Logcat.d("Http Response Code = " + code);
                if (code != HttpURLConnection.HTTP_OK) {
                    Logcat.e("Http Response Code = " + code);
                    throw new IOException();
                }

                if (data.getCallBackTarget() == null) {
                    Logcat.w("http data.target = null return ....");
                    return;
                }

                int length = connection.getContentLength();
                InputStream is = connection.getInputStream();
                int available = is.available();
//                Logcat.d("http InputStream available = " + available + "  Content Length =" + length);
                if (length <= 0 && available <= 0) {
                    return;
                }

                Object result = data.getResultType();
                if (String.class.getName().equals(result.getClass().getName())) {
                    //结果保存成文件的
                    String path = (String) data.getResultType();
                    File file = new File(path);
                    if (!file.exists()) {
                        //文件没有存在
                        File parentFile = file.getParentFile();
                        if (!parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    boolean state = IoUtils.pipReadWrite(is, fos, false);
                    if (state) {
                        if (callBack != null) {
                            data.setResultData(path);
                            callBack.notifySuccessMessage(data);
                        }
                    } else {
                        throw new IOException();
                    }
                    fos.close();
                } else {
                    byte[] buffer = IoUtils.tryRead(is);
                    if (byte[].class.getName().equals(((Class) result).getName()) || result == null) {
                        if (callBack != null) {
                            data.setResultData(buffer);
                            callBack.notifySuccessMessage(data);
                        }
                    } else {
                        String str = new String(buffer, "UTF-8");
                        String className = data.getResultType().toString().replace("class ", "");
                        Object entity = null;
                        Logcat.d("==> Request address = " + data.getAddress());
                        Logcat.d("==> Request to return the content = " + str);
                        if (str.startsWith("<?xml")) {
//                            entity = XmlParser.parserToEntity(str, className);
                        } else {
                            entity = JsonUtils.toEntity(className, str);
                        }
                        if (callBack != null) {
                            data.setResultData(entity == null ? buffer : entity);
                            callBack.notifySuccessMessage(data);
                        }
                    }
                }

            } catch (Throwable e) {
                e.printStackTrace();
                ConsumerTaskExecutor consumerTaskExecutor = container.getTaskExecutor();
                if (callBack != null && consumerTaskExecutor.getLoopState()) {
                    data.setResultData(null);
                    callBack.notifyErrorMessage(data);
                }
            } finally {
                connection.disconnect();
            }
        }
    }
}
