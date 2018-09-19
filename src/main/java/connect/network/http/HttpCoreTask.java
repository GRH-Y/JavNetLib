package connect.network.http;


import connect.network.base.RequestEntity;
import task.executor.BaseConsumerTask;
import util.IoUtils;
import util.Logcat;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
                httpsURLConnection.setHostnameVerifier(mConfig.getSslFactory().getHostnameVerifier());
                httpsURLConnection.setSSLSocketFactory(mConfig.getSslFactory().getSSLSocketFactory());
                connection = httpsURLConnection;
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }

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
    protected void onCreateData() {
        mConfig.onCheckIsIdle();
    }

    @Override
    protected void onProcess() {
        requestData(mConfig.popCacheData());
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
            mConfig.onCallBackError(submitEntity);
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
            if (code != HttpURLConnection.HTTP_OK) {
                Logcat.e("Http Response Code = " + code);
                mConfig.onCallBackError(submitEntity);
                return;
            }

            if (submitEntity.getCallBackTarget() == null) {
                Logcat.w("http data.target = null return ....");
                return;
            }

            int length = connection.getContentLength();
            InputStream is = connection.getInputStream();
            int available = is.available();
            if (length <= 0 && available <= 0) {
                mConfig.onCallBackError(submitEntity);
                return;
            }

            //拦截请求
            if (mConfig.intercept(submitEntity)) {
                return;
            }

            Class resultCls = (Class) submitEntity.getResultType();

            if (resultCls != null && resultCls.isAssignableFrom(String.class)) {
                //结果保存成文件的
                String path = (String) submitEntity.getResultType();
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
                //拦截请求
                if (mConfig.interceptCallBack(submitEntity, path)) {
                    return;
                }
                if (state) {
                    mConfig.onCallBackSuccess(path, submitEntity);
                } else {
                    mConfig.onCallBackError(submitEntity);
                }
                fos.close();
            } else {
                byte[] buffer = IoUtils.tryRead(is);
                if (resultCls == null || resultCls.isAssignableFrom(byte[].class)) {
                    mConfig.onCallBackSuccess(buffer, submitEntity);
                } else {
                    String result = new String(buffer, "UTF-8");
                    Logcat.d("==> Request address = " + submitEntity.getAddress());
                    Logcat.d("==> Request to return the content = " + result);
                    Object entity = mConfig.onConvertResult(resultCls, result);
                    //拦截请求
                    if (mConfig.interceptCallBack(submitEntity, entity)) {
                        return;
                    }
                    mConfig.onCallBackSuccess(entity == null ? buffer : entity, submitEntity);
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
            mConfig.onCallBackError(submitEntity);
        } finally {
            connection.disconnect();
        }
    }
}
