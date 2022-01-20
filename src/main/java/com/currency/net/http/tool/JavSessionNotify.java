package com.currency.net.http.tool;


import com.currency.net.base.joggle.ISessionNotify;
import com.currency.net.http.RequestEntity;
import log.LogDog;
import util.ReflectionCall;

/**
 * 网络请求会话回调
 * Created by Dell on 8/7/2017.
 *
 * @author yyz
 */
public class JavSessionNotify implements ISessionNotify {

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
    public void notifyProcess(RequestEntity request, int bytesRead, int contentLength) {
        notifyProcessImp(request, bytesRead, contentLength);
    }

    protected void notifyDataImp(RequestEntity request) {
        if (request.getCallBackTarget() == null) {
            LogDog.w("{JavSessionCallBack} CallBackTarget is null , do not dispose Message !!!");
            return;
        }
        String methodName = request.getException() == null && request.getRespondEntity() != null ? request.getSuccessMethod() : request.getErrorMethod();
        ReflectionCall.invoke(request.getCallBackTarget(), methodName, new Class[]{RequestEntity.class}, request);
    }


    protected void notifyProcessImp(RequestEntity request, int process, int maxProcess) {
        String methodName = request.getProcessMethod();
        ReflectionCall.invoke(request.getCallBackTarget(), methodName, new Class[]{RequestEntity.class, int.class, int.class}, request, process, maxProcess);
    }

}