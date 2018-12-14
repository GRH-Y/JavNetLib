package connect.network.http;


import connect.network.base.RequestEntity;
import connect.network.base.joggle.ISessionCallBack;
import connect.network.http.joggle.IRequestIntercept;
import connect.network.http.joggle.IResponseConvert;
import connect.network.http.joggle.POST;
import task.executor.BaseConsumerTask;
import task.executor.joggle.ILoopTaskExecutor;
import util.IoUtils;
import util.LogDog;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpCoreTask extends BaseConsumerTask<RequestEntity> {

    private HttpTaskConfig mConfig;

    protected HttpCoreTask(HttpTaskConfig config) {
        mConfig = config;
    }

    private HttpURLConnection init(RequestEntity task) {
        HttpURLConnection connection = null;
        try {
            String address = task.getAddress();
            URL url = new URL(address);

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
                connection.setInstanceFollowRedirects(false);
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setConnectTimeout(mConfig.getTimeout());
            connection.setReadTimeout(mConfig.getTimeout());
            connection.setRequestMethod(task.getRequestMethod());
            connection.setUseCaches(false);

            connection.setRequestProperty("Charset", "utf-8");
            connection.setRequestProperty("User-Agent", "JavHttpConnect-1.0");
            //此处为暴力方法设置接受所有类型，以此来防范返回415;
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Accept-Charset", "utf-8");
            connection.setRequestProperty("Accept-Encoding", "gzip");

            if (POST.class.getSimpleName().equals(task.getRequestMethod())) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
            }

            setRequestProperty(connection, mConfig.getRequestProperty());
            setRequestProperty(connection, task.getProperty());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 设置协议头
     *
     * @param connection
     * @param property
     */
    private void setRequestProperty(HttpURLConnection connection, Map<String, String> property) {
        if (property != null) {
            for (String key : property.keySet()) {
                try {
                    connection.setRequestProperty(key, property.get(key));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreateData() {
        ILoopTaskExecutor executor = mConfig.getExecutor();
        if (mConfig.getAttribute().getCacheDataSize() == 0) {
            executor.waitTask(mConfig.getFreeExitTime());
            if (mConfig.getAttribute().getCacheDataSize() == 0) {
                executor.stopTask();
            }
        }
    }

    @Override
    protected void onProcess() {
        requestData(mConfig.popCacheData());
    }

    protected void onResultCallBack(RequestEntity submitEntity) {
        ILoopTaskExecutor executor = mConfig.getExecutor();
        ISessionCallBack callBack = mConfig.getSessionCallBack();
        if (callBack != null && executor.getLoopState()) {
            callBack.notifyMessage(submitEntity);
        }
    }


    /**
     * 发送时转换编码，解决编码问题
     * String sendData = URLEncoder.encode(String.valueOf(enCodeData.sendData), "utf-8");
     * OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
     * writer.write(sendData);
     * writer.close();
     */

    private void requestData(RequestEntity submitEntity) {
        HttpURLConnection connection = init(submitEntity);
        if (connection == null) {
            onResultCallBack(submitEntity);
            return;
        }
        try {
            if (submitEntity.getSendData() != null) {
                OutputStream os = connection.getOutputStream();
                os.write(submitEntity.getSendData());
                os.flush();
            }

            int code = connection.getResponseCode();
//                Logcat.d("Http Response Code = " + code);
            LogDog.d("==> Request address = " + submitEntity.getAddress());
            if (code != HttpURLConnection.HTTP_OK) {
                LogDog.e("Http Response Code = " + code);
                onResultCallBack(submitEntity);
                return;
            }

            if (submitEntity.getCallBackTarget() == null) {
                LogDog.w("http data.target = null return ....");
                return;
            }

            int length = connection.getContentLength();
            InputStream is = connection.getInputStream();
            int available = is.available();
            byte[] buffer;
            if (length <= 0 && available <= 0) {
                buffer = IoUtils.tryRead(is);
            } else {
                buffer = IoUtils.tryRead(is);
            }
            if (buffer == null) {
                onResultCallBack(submitEntity);
                return;
            }

            //拦截请求
            IRequestIntercept intercept = mConfig.getInterceptRequest();
            if (intercept != null && intercept.intercept(submitEntity)) {
                return;
            }

            Class resultCls = (Class) submitEntity.getResultType();

            //转换响应数据
            Object entity = buffer;
            IResponseConvert convert = mConfig.getConvertResult();
            if (convert != null) {
                entity = convert.handlerEntity(resultCls, buffer);
            }

            //拦截请求
            if (intercept != null && intercept.interceptCallBack(submitEntity, entity)) {
                return;
            }
            submitEntity.setResultData(entity == null ? buffer : entity);
            onResultCallBack(submitEntity);

        } catch (Throwable e) {
            e.printStackTrace();
            onResultCallBack(submitEntity);
        } finally {
            connection.disconnect();
        }
    }
}
