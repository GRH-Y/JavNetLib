package connect.network.base.joggle;


import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;

/**
 * 会话回调接口
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */

public interface IXSessionNotify {

    /**
     * 通知结果
     *
     * @param request
     */
    void notifyData(XRequest request, XResponse response, Throwable e);

//    /**
//     * 通知处理过程状态
//     *
//     * @param request
//     * @param bytesRead     已读内容大小
//     * @param contentLength 内容的大小
//     */
//    void notifyProcess(RequestEntity request, int bytesRead, int contentLength);
}
