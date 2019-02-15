package connect.network.base;


import connect.network.base.joggle.ISessionCallBack;
import util.ThreadAnnotation;

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
    public void notifyData(RequestEntity entity) {
        String methodName;
        Object resultData;
        if (entity.getResultData() == null) {
            methodName = entity.getErrorMethodName();
            resultData = entity;
        } else {
            methodName = entity.getSuccessMethodName();
            resultData = entity.getResultData();
        }
        ThreadAnnotation.disposeMessage(methodName, entity.getCallBackTarget(), resultData);
    }

    @Override
    public void notifyProcess(RequestEntity entity, int process, int maxProcess, boolean isOver) {
        String methodName = entity.getProcessMethodName();
        ThreadAnnotation.disposeMessage(methodName, entity.getCallBackTarget(), process, maxProcess, isOver);
    }

}