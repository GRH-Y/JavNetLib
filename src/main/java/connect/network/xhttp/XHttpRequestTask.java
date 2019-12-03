package connect.network.xhttp;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioReceive;
import connect.network.xhttp.entity.XHttpRequest;
import connect.network.xhttp.entity.XHttpResponse;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpIntercept;
import util.ThreadAnnotation;
import util.joggle.JavKeep;

import java.nio.channels.SocketChannel;

public class XHttpRequestTask extends NioClientTask {

    private HttpProtocol httpProtocol;
    private XHttpRequest request;
    private XHttpConfig httpConfig;

    public XHttpRequestTask(XHttpRequest request) {
        this.request = request;
        setSender(new XHttpSender(this));
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
        if (data == null && e == null) {
            NioHPCClientFactory.getFactory().removeTask(this);
            return;
        }
        request.setException(e);
        request.setRespondData(data);
        IXHttpIntercept intercept = httpConfig.getIntercept();
        if (intercept != null) {
            boolean isIntercept = intercept.onRequestInterceptResult(request);
            if (isIntercept) {
                return;
            }
        }
        XHttpResponse response = XHttpResponse.parsing(data);
        String methodName = request.getException() != null ? request.getErrorMethod() : request.getSuccessMethod();
        ThreadAnnotation.disposeMessage(methodName, request.getCallBackTarget(), new Class[]{XHttpRequest.class, XHttpResponse.class}, request, response);
    }

}
