package connect.network.xhttp;

import connect.network.aio.AioClientTask;
import connect.network.aio.AioReceiver;
import connect.network.aio.AioSender;
import connect.network.base.AbsNetFactory;
import connect.network.base.joggle.INetReceiver;
import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;
import connect.network.base.joggle.IXSessionNotify;
import connect.network.nio.buf.MultilevelBuf;
import connect.network.xhttp.config.XHttpConfig;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpResponseConvert;
import connect.network.xhttp.utils.XHttpProtocol;
import connect.network.xhttp.utils.XResponseHelper;
import connect.network.xhttp.utils.XUrlMedia;
import log.LogDog;

import java.io.IOException;
import java.net.InetSocketAddress;

public class XAioHttpTask extends AioClientTask implements ISenderFeedback, INetReceiver<XResponse> {

    private XRequest request;
    private XHttpConfig httpConfig;
    private AbsNetFactory netFactory;
    private XHttpProtocol httpProtocol;

    private boolean isRedirect = false;


    XAioHttpTask(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
        if (request == null) {
            throw new NullPointerException("request is null !!!");
        }
        this.httpConfig = httpConfig;
        this.netFactory = netFactory;

        httpProtocol = new XHttpProtocol();
        initTask(request);
    }

    protected void initTask(XRequest request) {
        this.request = request;
        httpProtocol.initProtocol(request);
        XUrlMedia httpUrlMedia = request.getUrl();
        String host = httpUrlMedia.getHost();
//        IXHttpDns httpDns = httpConfig.getXHttpDns();
//        if (httpDns != null) {
//            host = httpDns.findCacheDns(httpUrlMedia.getHost());
//        }
        setAddress(host, httpUrlMedia.getPort());
        setTLS(httpUrlMedia.isTSL());
    }

    @Override
    public void onSenderFeedBack(INetSender sender, Object data, Throwable e) {
        if (e != null) {
            netFactory.removeTask(this);
        }
        if (data instanceof MultilevelBuf) {
            XMultiplexCacheManger.getInstance().lose((MultilevelBuf) data);
        }
    }


    @Override
    public void onReceiveFullData(XResponse response, Throwable e) {
        if (e != null) {
            IXSessionNotify sessionNotify = httpConfig.getSessionNotify();
            if (sessionNotify != null) {
                sessionNotify.notifyData(request, null, e);
            }
        } else {
            int code = XResponseHelper.getCode(response);
            if (code >= 300 && code < 400) {
                //重定向
                String location = response.getHeadForKey(XHttpProtocol.XY_LOCATION);
                request.setUrl(location);
                isRedirect = true;
            } else {
                IXHttpResponseConvert responseConvert = httpConfig.getResponseConvert();
                if (responseConvert != null) {
                    responseConvert.handlerEntity(request, response);
                }
                IXSessionNotify sessionNotify = httpConfig.getSessionNotify();
                if (sessionNotify != null) {
                    sessionNotify.notifyData(request, response, null);
                }
            }
        }
        netFactory.removeTask(XAioHttpTask.this);
    }

    @Override
    protected void onConnectCompleteChannel() {
        AioReceiver receiver = getReceiver();
        AioSender sender = getSender();
        if (sender == null) {
            sender = new AioSender(getSocketChannel());
            setSender(sender);
        } else {
            sender.setChannel(getSocketChannel());
        }
        if (receiver == null) {
            receiver = new AioReceiver(this);
//            receiver.setDataReceiver(this);
            setReceiver(receiver);
        } else {
            receiver.setClientTask(this);
        }

        XUrlMedia httpUrlMedia = request.getUrl();
        IXHttpDns httpDns = httpConfig.getXHttpDns();
        if (httpDns != null) {
            try {
                InetSocketAddress address = (InetSocketAddress) getSocketChannel().getRemoteAddress();
                httpDns.setCacheDns(httpUrlMedia.getHost(), address.getAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (httpUrlMedia.isTSL()) {
            sender.setTlsHandler(getTlsHandler());
            receiver.setTlsHandler(getTlsHandler());
        }
        receiver.triggerReceiver();
        byte[] head = httpProtocol.toByte();
        sender.setSenderFeedback(this);
        sender.sendData(head);
        sender.sendData(request.getSendData());
        LogDog.d("==> head = " + new String(head));
    }

    @Override
    protected void onCloseClientChannel() {
        try {
            InetSocketAddress address = (InetSocketAddress) getSocketChannel().getRemoteAddress();
            LogDog.d("==> XAioHttpTask onCloseClientChannel host = " + address.getHostName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        if (isRedirect) {
            //是否是重定向
            initTask(request);
            netFactory.addTask(this);
            isRedirect = false;
        } else {
            //移除任务记录
            XMultiplexCacheManger.getInstance().lose(this);
        }
        LogDog.d("==> XAioHttpTask onRecovery !!! ");
    }
}
