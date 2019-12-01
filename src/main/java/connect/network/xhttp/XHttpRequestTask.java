package connect.network.xhttp;

import connect.network.http.RequestEntity;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceive;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpIntercept;
import util.ThreadAnnotation;
import util.joggle.JavKeep;

import java.nio.channels.SocketChannel;

public class XHttpRequestTask extends NioClientTask {

    private HttpProtocol httpProtocol;
    private RequestEntity request;
    private XHttpConfig httpConfig;

    public XHttpRequestTask(RequestEntity request) {
        this.request = request;
        setSender(new HttpSender(this));
        setReceive(new NioReceive(this, "onReceiveData"));
        httpProtocol = new HttpProtocol(request);
        httpProtocol.setUserParameter(request.getRequestProperty());
        httpConfig = XHttpConnect.getInstance().getHttpConfig();
        IXHttpDns httpDns = httpConfig.getXHttpDns();
        String address = httpDns.findCacheDns(request.getAddress());
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
        }
    }

    @JavKeep
    private void onReceiveData(byte[] data, Exception e) {
        request.setException(e);
        request.setRespondData(data);
        IXHttpIntercept intercept = httpConfig.getIntercept();
        if (intercept != null) {
            boolean isIntercept = intercept.onRequestInterceptResult(request);
            if (isIntercept) {
                return;
            }
        }
        String methodName = request.getException() != null ? request.getErrorMethod() : request.getSuccessMethod();
        ThreadAnnotation.disposeMessage(methodName, request.getCallBackTarget(), new Class[]{RequestEntity.class}, request);
    }

}
