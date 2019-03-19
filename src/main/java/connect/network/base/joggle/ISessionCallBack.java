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
     * 获取回调监听者
     *
     * @return 回调监听者
     */
    Object getCallBackTarget();

    /**
     * 设置回调监听者
     *
     * @param target
     */
    void setCallBackTarget(Object target);


    /**
     * 通知结果
     *
     * @param entity
     */
    void notifyData(RequestEntity entity);

    /**
     * 通知处理过程状态
     * @param entity
     * @param process
     * @param maxProcess
     * @param isOver
     */
    void notifyProcess(RequestEntity entity,int process, int maxProcess, boolean isOver);
}
