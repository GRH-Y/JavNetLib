package connect.network.base.joggle;


import connect.network.http.RequestEntity;

/**
 * 会话回调接口
 * Created by Dell on 8/8/2017.
 *
 * @author yyz
 */

public interface ISessionCallBack {

    /**
     * 通知结果
     *
     * @param request
     */
    void notifyData(RequestEntity request);

    /**
     * 通知处理过程状态
     * @param request
     * @param process
     * @param maxProcess
     * @param isOver
     */
    void notifyProcess(RequestEntity request,int process, int maxProcess, boolean isOver);
}
