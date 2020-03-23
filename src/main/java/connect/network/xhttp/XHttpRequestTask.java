package connect.network.xhttp;

import connect.network.base.AbsNetFactory;
import connect.network.base.joggle.INetReceive;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioSender;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;
import connect.network.xhttp.entity.XResponseHelper;
import connect.network.xhttp.entity.XUrlMedia;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpIntercept;
import connect.network.xhttp.joggle.IXHttpResponseConvert;
import log.LogDog;
import util.ReflectionCall;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class XHttpRequestTask extends NioClientTask {

    private XRequest request;
    private XHttpConfig httpConfig;
    private AbsNetFactory netFactory;
    private XHttpProtocol httpProtocol;

    private boolean isRedirect = false;


    XHttpRequestTask(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
        if (request == null) {
            throw new NullPointerException("request is null !!!");
        }
        this.request = request;
        this.httpConfig = httpConfig;
        this.netFactory = netFactory;

        httpProtocol = new XHttpProtocol();
        initTask(request);
    }

    protected void initTask(XRequest request) {
        httpProtocol.init(request);
        IXHttpDns httpDns = httpConfig.getXHttpDns();
        XUrlMedia httpUrlMedia = request.getUrl();
        String host = httpUrlMedia.getHost();
        if (httpDns != null) {
            host = httpDns.findCacheDns(httpUrlMedia.getHost());
        }
        setAddress(host, httpUrlMedia.getPort(), httpUrlMedia.isTSL());
    }


    class ReceiveCallBack implements INetReceive<XResponse> {

        @Override
        public void onReceive(XResponse response, Exception e) {
            if (e != null) {
                ReflectionCall.invoke(request.getCallBackTarget(), request.getErrorMethod(),
                        new Class[]{XRequest.class, XResponse.class}, request, response);
                netFactory.removeTask(XHttpRequestTask.this);
                return;
            }
            IXHttpIntercept intercept = httpConfig.getIntercept();
            if (intercept != null) {
                boolean isIntercept = intercept.onRequestInterceptResult(request);
                if (isIntercept) {
                    netFactory.removeTask(XHttpRequestTask.this);
                    return;
                }
            }
            int code = XResponseHelper.getCode(response);
            if (code >= 300 && code < 400) {
                //重定向
                String location = response.getHeadForKey(XHttpProtocol.XY_LOCATION);
                request.setUrl(location);
                initTask(request);
                isRedirect = true;
            } else {
                IXHttpResponseConvert responseConvert = httpConfig.getResponseConvert();
                if (responseConvert != null) {
                    responseConvert.handlerEntity(request, response);
                }
                ReflectionCall.invoke(request.getCallBackTarget(), request.getSuccessMethod(),
                        new Class[]{XRequest.class, XResponse.class}, request, response);
            }
            netFactory.removeTask(XHttpRequestTask.this);
        }
    }

    @Override
    protected void onConnectCompleteChannel(boolean isConnect, SocketChannel channel, SSLEngine sslEngine) {
        if (isConnect) {
            ReceiveCallBack receiveCallBack = new ReceiveCallBack();
            XUrlMedia httpUrlMedia = request.getUrl();
            if (httpUrlMedia.isTSL()) {
                setSender(new XHttpsSender(getTlsHandler()));
                setReceive(new XHttpsReceive(getTlsHandler(), receiveCallBack));
            } else {
                setSender(new NioSender(channel));
                setReceive(new XHttpReceive(receiveCallBack));
            }
            byte[] head = httpProtocol.toByte();
            try {
                getSender().sendData(head);
                getSender().sendData(request.getSendData());
            } catch (IOException e) {
                netFactory.removeTask(this);
            }
            LogDog.d("==> head = " + new String(head));
        }
    }


    @Override
    protected void onRecovery() {
        if (isRedirect) {
            //是否是重定向
            setTaskNeedClose(false);
            netFactory.addTask(this);
            isRedirect = false;
        } else {
            XHttpReceive receive = getReceive();
            if (receive != null) {
                receive.reset();
            }
            //移除任务记录
            XHttpRequestTaskManger.getInstance().removerTask(request.toString());
        }
    }
}
