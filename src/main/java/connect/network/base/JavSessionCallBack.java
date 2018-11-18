package connect.network.base;


import connect.network.base.joggle.ISessionCallBack;
import task.message.ThreadAnnotation;

/**
 * 网络请求会话回调
 * Created by Dell on 8/7/2017.
 *
 * @author yyz
 */
public class JavSessionCallBack implements ISessionCallBack {

    protected Object target = null;

    public JavSessionCallBack(Object target) {
        setCallBackTarget(target);
    }

    public JavSessionCallBack() {
    }

    @Override
    public Object getCallBackTarget() {
        return target;
    }

    @Override
    public void setCallBackTarget(Object target) {
        this.target = target;
    }


    /**
     * 网络请求成功回调
     *
     * @param entity 请求任务
     */
    @Override
    public void notifySuccessMessage(RequestEntity entity) {
        Object object = entity == null || entity.getCallBackTarget() == null ? target : entity.getCallBackTarget();
        ThreadAnnotation.disposeMessage(entity.getSuccessMethodName(), object, entity);
    }


    /**
     * 网络请求失败回调
     *
     * @param entity 请求任务
     */
    @Override
    public void notifyErrorMessage(RequestEntity entity) {
        Object object = entity == null || entity.getCallBackTarget() == null ? target : entity.getCallBackTarget();
        ThreadAnnotation.disposeMessage(entity.getErrorMethodName(), object, entity);
    }

    @Override
    public void recycle() {
        target = null;
    }

}