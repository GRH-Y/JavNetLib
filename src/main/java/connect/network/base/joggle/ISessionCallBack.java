package connect.network.base.joggle;


import connect.network.base.RequestEntity;

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
     * 成功通知
     *
     * @param entity
     */
    void notifyMessage(RequestEntity entity);


}
