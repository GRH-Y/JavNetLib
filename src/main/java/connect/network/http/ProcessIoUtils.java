package connect.network.http;

import connect.network.base.joggle.ISessionCallBack;
import util.IoUtils;

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
                                       RequestEntity requestEntity, ISessionCallBack callBack) {
        boolean state = true;
        int missCount = 0;
        while (state) {
            try {
                int available = is.available();
                available = available > 0 ? available : IoUtils.SIZE;
                byte[] buffer = new byte[available];
                int len = is.read(buffer);
                if (len > 0) {
                    os.write(buffer, 0, len);
                    os.flush();
                    if (callBack != null) {
                        callBack.notifyProcess(requestEntity, len, maxLength, false);
                    }
                } else {
                    if (missCount < 8 && isCanTimeOut) {
                        missCount++;
                    } else {
                        break;
                    }
                }
            } catch (Throwable e) {
                if (e instanceof SocketTimeoutException) {
                    state = missCount < 8 && isCanTimeOut;
                    missCount++;
                } else {
                    state = false;
                    e.printStackTrace();
                }
            }
        }
        if (callBack != null) {
            callBack.notifyProcess(requestEntity, 0, maxLength, true);
        }
        return state;
    }


}
