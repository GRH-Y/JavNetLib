package connect.network.http;


import connect.network.base.RequestMode;
import connect.network.base.joggle.ISessionCallBack;
import connect.network.http.joggle.IRequestIntercept;
import connect.network.http.joggle.IResponseConvert;
import log.LogDog;
import storage.FileHelper;
import task.executor.BaseConsumerTask;
import task.executor.joggle.ILoopTaskExecutor;
import util.IoEnvoy;
import util.StringEnvoy;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpCoreTask extends BaseConsumerTask {

    private HttpTaskConfig mConfig;

    protected HttpCoreTask(HttpTaskConfig config) {
        mConfig = config;
    }

    /**
     * 创建链接
     *
     * @param task
     * @return
     * @throws Exception
     */
    private HttpURLConnection createHttpConnect(RequestEntity task) throws Exception {
        HttpURLConnection connection;
        String address = mConfig.getBaseUrl() == null || task.isDisableBaseUrl() ? task.getAddress() : mConfig.getBaseUrl() + task.getAddress();
        URL url = new URL(address);
        LogDog.d("{HttpCoreTask} Request address = " + url.toString());

        if (address.startsWith("https")) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            if (mConfig.getSslFactory() != null) {
                if (mConfig.getSslFactory().getHostnameVerifier() != null) {
                    httpsURLConnection.setHostnameVerifier(mConfig.getSslFactory().getHostnameVerifier());
                }
                if (mConfig.getSslFactory().getSSLSocketFactory() != null) {
                    httpsURLConnection.setSSLSocketFactory(mConfig.getSslFactory().getSSLSocketFactory());
                }
            }
            connection = httpsURLConnection;
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        return connection;
    }


    /**
     * 配置连接
     *
     * @param connection
     * @param task
     * @throws Exception
     */
    private void configHttpConnect(HttpURLConnection connection, RequestEntity task) throws Exception {
        connection.setConnectTimeout(mConfig.getTimeout());
        connection.setReadTimeout(mConfig.getTimeout());
        connection.setRequestMethod(task.getRequestMode().getMode());
        connection.setUseCaches(false);

        connection.setRequestProperty("Charset", "utf-8");
        connection.setRequestProperty("User-Agent", "JavHttpConnect-1.0");
        //此处为暴力方法设置接受所有类型，以此来防范返回415;
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Charset", "utf-8");
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

        if (RequestMode.POST == task.getRequestMode()) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", HttpTaskConfig.CONTENT_TYPE_JSON);
        }
        setRequestProperty(connection, mConfig.getGlobalRequestProperty());
        setRequestProperty(connection, task.getRequestProperty());
    }


    /**
     * 设置协议头
     *
     * @param connection
     * @param property
     */
    private void setRequestProperty(HttpURLConnection connection, Map<String, Object> property) {
        if (property != null) {
            for (String key : property.keySet()) {
                try {
                    Object obj = property.get(key);
                    if (obj != null && isBasicDataType(obj.getClass())) {
                        String value;
                        if (obj instanceof String) {
                            value = (String) obj;
                        } else {
                            value = String.valueOf(obj);
                        }
                        connection.setRequestProperty(key, value);
                        LogDog.d("==> key = " + key + " value = " + value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean isBasicDataType(Class clx) {
        return clx == Integer.class || clx == int.class || clx == Long.class || clx == long.class
                || clx == Double.class || clx == double.class || clx == Float.class || clx == float.class
                || clx == Boolean.class || clx == boolean.class || clx == char.class || clx == Character.class
                || clx == String.class;
    }

    @Override
    protected void onCreateData() {
        ILoopTaskExecutor executor = mConfig.getExecutor();
        if (executor != null && mConfig.getAttribute().getCacheDataSize() == 0) {
            executor.waitTask(mConfig.getFreeExitTime());
            if (mConfig.getAttribute().getCacheDataSize() == 0) {
                executor.stopTask();
            }
        }
    }

    @Override
    protected void onProcess() {
        RequestEntity entity = mConfig.popCacheData();
        if (entity != null) {
            requestData(entity);
        }
    }

    /**
     * 结果回调处理
     *
     * @param submitEntity
     */
    private void onResultCallBack(RequestEntity submitEntity) {
        //拦截结果
        IRequestIntercept intercept = mConfig.getInterceptRequest();
        if (intercept != null) {
            if (intercept.onRequestInterceptResult(submitEntity)) {
                return;
            }
        }
        ISessionCallBack callBack = mConfig.getSessionCallBack();
        if (callBack != null) {
            callBack.notifyData(submitEntity);
        }
    }

    /**
     * 拦截请求
     *
     * @param submitEntity
     * @return
     */
    private boolean onInterceptRequest(RequestEntity submitEntity) {
        //拦截请求
        IRequestIntercept intercept = mConfig.getInterceptRequest();
        return intercept != null && intercept.onStartRequestIntercept(submitEntity);
    }

    private void requestData(RequestEntity submitEntity) {
        //拦截请求
        if (onInterceptRequest(submitEntity)) {
            return;
        }
        HttpURLConnection connection = null;
        try {
            connection = createHttpConnect(submitEntity);
            configHttpConnect(connection, submitEntity);
            if (submitEntity.getSendData() != null) {
//                byte[] gzip = GZipUtils.compress(submitEntity.getSendData());
                IoEnvoy.writeToFull(connection.getOutputStream(), submitEntity.getSendData(), true);
                LogDog.d("{HttpCoreTask} Post submitEntity = " + new String(submitEntity.getSendData()));
            }
            int code = connection.getResponseCode();
            int length = connection.getContentLength();
            LogDog.w("{HttpCoreTask} Http Response Code = " + code + " length = " + length);

            if (code == HttpURLConnection.HTTP_OK) {
                String encode = connection.getHeaderField("Content-Encoding");
                InputStream is = connection.getInputStream();
                Object resultType = submitEntity.getResultType();
                //保存响应头参数
                submitEntity.setResponseProperty(connection.getHeaderFields());

                if (resultType.getClass().isAssignableFrom(String.class)) {
                    //目录结果为字符串说明是下载文件
                    File file = FileHelper.crateFile((String) resultType);
                    if (file != null) {
                        FileOutputStream fileStream = new FileOutputStream(file);
                        boolean state = ProcessIoUtils.pipReadWrite(is, fileStream, true, length, submitEntity, mConfig.getSessionCallBack());
                        if (state) {
                            submitEntity.setRespondEntity(resultType);
                        }
                    }
                } else {
                    byte[] buffer = IoEnvoy.tryRead(is);
                    submitEntity.setRespondData(buffer);
                    //转换成对应的数据
                    Object entity = null;
                    IResponseConvert convert = mConfig.getConvertResult();
                    if (convert != null) {
                        entity = convert.handlerEntity((Class) resultType, buffer, encode);
                    }
                    if (entity == null) {
                        submitEntity.setRespondData(buffer);
                    }
                    submitEntity.setRespondEntity(entity);
                }
                onResultCallBack(submitEntity);
            } else if (code >= HttpURLConnection.HTTP_MULT_CHOICE && code <= HttpURLConnection.HTTP_USE_PROXY) {
                //重定向
                String newUrl = connection.getHeaderField("Location");
                if (StringEnvoy.isNotEmpty(mConfig.getBaseUrl())) {
                    newUrl.replace(mConfig.getBaseUrl(), "");
                }
                submitEntity.setAddress(newUrl);
                mConfig.getAttribute().pushToCache(submitEntity);
            } else {
                InputStream is = connection.getInputStream();
                byte[] buffer = IoEnvoy.tryRead(is);
                submitEntity.setRespondData(buffer);
                submitEntity.setResponseCode(code);
                onResultCallBack(submitEntity);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            submitEntity.setException(e);
            onResultCallBack(submitEntity);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
