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
import util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
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
            String address = mConfig.getBaseUrl() == null ? task.getAddress() : mConfig.getBaseUrl() + task.getAddress();
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
                connection.setRequestProperty("Content-Type", HttpTaskConfig.CONTENT_TYPE_JSON);
            }

            setRequestProperty(connection, mConfig.getGlobalRequestProperty());
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
        RequestEntity entity = mConfig.popCacheData();
        if (entity != null) {
            requestData(entity);
        }
    }

    protected void onResultCallBack(RequestEntity submitEntity) {
        ISessionCallBack callBack = mConfig.getSessionCallBack();
        if (callBack != null) {
            callBack.notifyData(submitEntity);
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
            int length = connection.getContentLength();

            LogDog.d("==> Request address = " + (StringUtils.isEmpty(mConfig.getBaseUrl()) ? submitEntity.getAddress() : mConfig.getBaseUrl() + submitEntity.getAddress()));
            if (submitEntity.getSendData() != null) {
                LogDog.d("==>JavHttpConnect post submitEntity = " + new String(submitEntity.getSendData()));
            }
            if (code == HttpURLConnection.HTTP_MOVED_TEMP) {
                String newUrl = connection.getHeaderField("Location");
                if (StringUtils.isNotEmpty(mConfig.getBaseUrl())) {
                    newUrl.replace(mConfig.getBaseUrl(), "");
                }
                submitEntity.setAddress(newUrl);
                mConfig.getAttribute().pushToCache(submitEntity);
                return;
            } else if (code != HttpURLConnection.HTTP_OK) {
                LogDog.e("Http Response Code = " + code);
                onResultCallBack(submitEntity);
                return;
            }

            if (submitEntity.getCallBackTarget() == null) {
                LogDog.w("http data.target = null return ....");
                return;
            }

            String encode = connection.getHeaderField("Content-Encoding");
            InputStream is = connection.getInputStream();
            Object resultType = submitEntity.getResultType();

            if (resultType.getClass().isAssignableFrom(String.class)) {
                //目录结果为字符串说明是下载文件
                File file = new File((String) resultType);
                File parentFile = file.getParentFile();
                boolean exists = parentFile.exists();
                if (!exists) {
                    exists = parentFile.mkdirs();
                }
                if (exists) {
                    FileOutputStream outputStream = new FileOutputStream(file);
                    boolean state = ProcessIoUtils.pipReadWrite(is, outputStream, false, length, submitEntity, mConfig.getSessionCallBack());
//                    boolean state = IoUtils.pipReadWrite(is, outputStream, false);
                    if (state) {
                        submitEntity.setResultData(resultType);
                    }
                }
                onResultCallBack(submitEntity);
            } else {
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

                //转换响应数据
                Object entity = buffer;
                IResponseConvert convert = mConfig.getConvertResult();
                if (convert != null) {
                    entity = convert.handlerEntity((Class) resultType, buffer, encode);
                }

                //拦截请求
                if (intercept != null && intercept.interceptCallBack(submitEntity, entity)) {
                    return;
                }
                submitEntity.setResultData(entity == null ? buffer : entity);
                onResultCallBack(submitEntity);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            onResultCallBack(submitEntity);
        } finally {
            connection.disconnect();
        }
    }
}
