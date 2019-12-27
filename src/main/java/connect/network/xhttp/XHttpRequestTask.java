package connect.network.xhttp;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import connect.network.xhttp.entity.XHttpRequest;
import connect.network.xhttp.entity.XHttpResponse;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpIntercept;
import log.LogDog;
import util.ReflectionCall;
import util.joggle.JavKeep;

import java.nio.channels.SocketChannel;

public class XHttpRequestTask extends NioClientTask {

    private HttpProtocol httpProtocol;
    private XHttpRequest request;
    private XHttpConfig httpConfig;


    public XHttpRequestTask(XHttpRequest request) {
        this.request = request;
        setSender(new XHttpSender(this));
        setReceive(new XHttpReceive(this, this, "onReceiveData"));
        httpProtocol = new HttpProtocol(request);
        httpConfig = XHttpConnect.getInstance().getHttpConfig();
        IXHttpDns httpDns = httpConfig.getXHttpDns();
        String address = httpDns.findCacheDns(request.getHost());
        setAddress(address, request.getPort());
    }

    @Override
    protected void onConfigSocket(boolean isConnect, SocketChannel channel) {
        if (isConnect) {
            IXHttpIntercept intercept = httpConfig.getIntercept();
            if (intercept != null) {
                boolean isIntercept = intercept.onStartRequestIntercept(request);
                if (isIntercept) {
                    return;
                }
            }
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
                return;
            }
        }
        String methodName = response.getException() != null ? request.getErrorMethod() : request.getSuccessMethod();
        ReflectionCall.invoke(request.getCallBackTarget(), methodName, new Class[]{XHttpRequest.class, XHttpResponse.class}, request, response);
        NioHPCClientFactory.getFactory().removeTask(this);
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        //移除任务记录
        XHttpTaskManger.getInstance().removerTask(request.toString());
    }
}
