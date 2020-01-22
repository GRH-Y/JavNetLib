package connect.network.xhttp;

import connect.network.nio.NioClientTask;
import connect.network.xhttp.entity.XHttpRequest;
import connect.network.xhttp.entity.XHttpResponse;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpIntercept;
import connect.network.xhttp.joggle.IXHttpResponseConvert;
import log.LogDog;
import util.ReflectionCall;
import util.joggle.JavKeep;

import java.nio.channels.SocketChannel;

public class XHttpRequestTask extends NioClientTask {

    private XHttpReceive xHttpReceive;
    private HttpProtocol httpProtocol;
    private XHttpRequest request;
    private XHttpConfig httpConfig;

    private boolean isRedirect = false;


    public XHttpRequestTask(XHttpRequest request) {
        this.request = request;
        xHttpReceive = new XHttpReceive(this, this, "onReceiveData");
        setReceive(xHttpReceive);
        httpConfig = XHttpConnect.getInstance().getHttpConfig();
        httpProtocol = new HttpProtocol();
        initTask(request);
    }

    public void initTask(XHttpRequest request) {
        httpProtocol.initHead(request);
        IXHttpDns httpDns = httpConfig.getXHttpDns();
        String address = httpDns.findCacheDns(request.getHost());
        setAddress(address, request.getPort());
    }

    @Override
    protected void onConfigSocket(boolean isConnect, SocketChannel channel) {
        if (isConnect) {
            setSender(new XHttpSender(this, channel));
            byte[] head = httpProtocol.toByte();
            getSender().sendData(head);
            getSender().sendData(request.getSendData());
            LogDog.d("==> head = " + new String(head));
        }
    }

    @JavKeep
    private void onReceiveData(XHttpResponse response) {
        IXHttpIntercept intercept = httpConfig.getIntercept();
        if (intercept != null) {
            boolean isIntercept = intercept.onRequestInterceptResult(request);
            if (isIntercept) {
                httpConfig.getNetFactory().removeTask(this);
                return;
            }
        }
        int code = response.getCode();
        if (code >= 300 && code < 400) {
            //重定向
            String location = response.getHeadForKey(HttpProtocol.XY_LOCATION);
            request.setAddress(location);
            httpProtocol.updateHeadParameter(HttpProtocol.XY_HOST, request.getHost());
            httpProtocol.updateHeadParameter(HttpProtocol.XY_REFERER, request.getReferer());
            httpProtocol.updatePath(request.getRequestMode().getMode(), request.getPath());
            isRedirect = true;
        } else {
            IXHttpResponseConvert responseConvert = httpConfig.getResponseConvert();
            if (responseConvert != null) {
                responseConvert.handlerEntity(request, response);
            }
            String methodName = response.getException() != null ? request.getErrorMethod() : request.getSuccessMethod();
            ReflectionCall.invoke(request.getCallBackTarget(), methodName, new Class[]{XHttpRequest.class, XHttpResponse.class}, request, response);
        }
        httpConfig.getNetFactory().removeTask(this);
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        xHttpReceive.reset();
        if (isRedirect) {
            //是否是重定向
            setTaskNeedClose(false);
            httpConfig.getNetFactory().addTask(this);
            isRedirect = false;
        } else {
            //移除任务记录
            XHttpTaskManger.getInstance().removerTask(request.toString());
        }
    }
}
