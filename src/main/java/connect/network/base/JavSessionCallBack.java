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
    public void notifyMessage(RequestEntity entity) {
        Object object = entity.getCallBackTarget() == null ? target : entity.getCallBackTarget();
        String methodName = entity.getResultData() != null ? entity.getSuccessMethodName() : entity.getErrorMethodName();
        ThreadAnnotation.disposeMessage(methodName, object, entity);
    }

}