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
     * @param request 请求任务
     */
    @Override
    public void notifyData(RequestEntity request) {
        notifyDataImp(request);
    }

    @Override
    public void notifyProcess(RequestEntity request, int process, int maxProcess, boolean isOver) {
        notifyProcessImp(request, process, maxProcess, isOver);
    }

    protected void notifyDataImp(RequestEntity request) {
        if (request.getCallBackTarget() == null) {
            LogDog.w("{JavSessionCallBack} CallBackTarget is null , do not dispose Message !!!");
            return;
        }
        String methodName = request.getException() != null ? request.getErrorMethod() : request.getSuccessMethod();
        ThreadAnnotation.disposeMessage(methodName, request.getCallBackTarget(), new Class[]{RequestEntity.class}, request);
    }


    protected void notifyProcessImp(RequestEntity request, int process, int maxProcess, boolean isOver) {
        String methodName = request.getProcessMethod();
        ThreadAnnotation.disposeMessage(methodName, request.getCallBackTarget(), new Class[]{int.class, int.class, boolean.class}, process, maxProcess, isOver);
    }

}