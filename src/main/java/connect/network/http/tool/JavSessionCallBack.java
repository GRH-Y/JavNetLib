package connect.network.http.tool;


import connect.network.base.joggle.ISessionCallBack;
import connect.network.http.RequestEntity;
import log.LogDog;
import util.ThreadAnnotation;

/**
 * 网络请求会话回调
 * Created by Dell on 8/7/2017.
 *
 * @author yyz
 */
public class JavSessionCallBack implements ISessionCallBack {

    /**
     * 网络请求成功回调
     *
     * @param entity 请求任务
     */
    @Override
    public void notifyData(RequestEntity entity) {
        notifyDataImp(entity);
    }

    @Override
    public void notifyProcess(RequestEntity entity, int process, int maxProcess, boolean isOver) {
        notifyProcessImp(entity, process, maxProcess, isOver);
    }

    protected void notifyDataImp(RequestEntity entity) {
        if (entity.getCallBackTarget() == null) {
            LogDog.w("{JavSessionCallBack} CallBackTarget is null , do not dispose Message !!!");
            return;
        }
        String methodName;
        Object[] resultData;
        if (entity.getRespondEntity() == null) {
            methodName = entity.getErrorMethodName();
            resultData = new Object[]{entity};
        } else {
            methodName = entity.getSuccessMethodName();
            resultData = new Object[]{entity, entity.getRespondEntity()};
        }
        ThreadAnnotation.disposeMessage(methodName, entity.getCallBackTarget(), resultData);
    }


    protected void notifyProcessImp(RequestEntity entity, int process, int maxProcess, boolean isOver) {
        String methodName = entity.getProcessMethodName();
        ThreadAnnotation.disposeMessage(methodName, entity.getCallBackTarget(), process, maxProcess, isOver);
    }

}