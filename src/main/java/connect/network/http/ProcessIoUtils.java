package connect.network.http;

import connect.network.base.joggle.ISessionNotify;
import log.LogDog;
import util.IoEnvoy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

public class ProcessIoUtils {


    /**
     * 堵塞式管道流，InputStream输入流不停往OutputStream输出流写数据
     *
     * @param is 输入流
     * @param os 输出流
     */
    public static boolean pipReadWrite(InputStream is, OutputStream os, boolean isCanTimeOut, int maxLength,
                                       RequestEntity requestEntity, ISessionNotify callBack) {
        boolean state = true;
        int missCount = 0;
        int currentLength = 0;
        LogDog.w("{ProcessIoUtils} pipReadWrite currentLength = " + currentLength);
        byte[] buffer = new byte[IoEnvoy.SIZE];
        do {
            try {
                int len = is.read(buffer);
                if (len > 0) {
                    os.write(buffer, 0, len);
                    os.flush();
                    missCount = 0;
                    currentLength += len;
                    if (callBack != null) {
                        callBack.notifyProcess(requestEntity, currentLength, maxLength);
                    }
                } else {
                    if (missCount < 8 && isCanTimeOut) {
                        missCount++;
                    } else {
                        throw new IOException("socket read fail !!! ");
                    }
                }
            } catch (Throwable e) {
                if (e instanceof SocketTimeoutException) {
                    state = missCount < 8 && isCanTimeOut;
                    missCount++;
                } else {
                    state = false;
                    requestEntity.setException(e);
                    e.printStackTrace();
                }
            }
        } while (currentLength < maxLength && state);

        return state;
    }

}
