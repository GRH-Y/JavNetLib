package connect.network.xhttp;

import connect.network.base.AbsNetFactory;
import connect.network.base.joggle.INetReceiver;
import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;
import connect.network.base.joggle.IXSessionNotify;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioSender;
import connect.network.nio.buf.MultilevelBuf;
import connect.network.xhttp.config.XHttpConfig;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpResponseConvert;
import connect.network.xhttp.utils.XHttpProtocol;
import connect.network.xhttp.utils.XResponseHelper;
import connect.network.xhttp.utils.XUrlMedia;
import util.StringEnvoy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class XNioHttpTask extends NioClientTask implements ISenderFeedback, INetReceiver<XResponse> {

    private XRequest request;
    private XHttpConfig httpConfig;
    private AbsNetFactory netFactory;
    private XHttpProtocol httpProtocol;

    private boolean isRedirect = false;


    XNioHttpTask(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
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
        IXHttpDns httpDns = httpConfig.getXHttpDns();
        if (httpDns != null) {
            String ip = httpDns.getCacheDns(httpUrlMedia.getHost());
            if (StringEnvoy.isNotEmpty(ip)) {
                host = ip;
            }
        }
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
        IXSessionNotify sessionNotify = httpConfig.getSessionNotify();
        if (e != null) {
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
                if (sessionNotify != null) {
                    sessionNotify.notifyData(request, response, null);
                }
            }
        }
        netFactory.removeTask(XNioHttpTask.this);
    }


    @Override
    protected void onConnectCompleteChannel(SocketChannel channel) throws IOException {
        XUrlMedia httpUrlMedia = request.getUrl();
        IXHttpDns httpDns = httpConfig.getXHttpDns();
        if (httpDns != null) {
            InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
            httpDns.setCacheDns(httpUrlMedia.getHost(), address.getAddress().getHostAddress());
        }
        XHttpReceiver receiver = getReceiver();
        NioSender sender = getSender();

        if (httpUrlMedia.isTSL()) {
            if (sender instanceof XHttpsSender) {
                XHttpsSender httpsSender = (XHttpsSender) sender;
                httpsSender.setTlsHandler(getTlsHandler());
                httpsSender.setChannel(selectionKey, channel);
            } else {
                setSender(new XHttpsSender(getTlsHandler(), selectionKey, channel));
            }
            if (receiver instanceof XHttpsReceiver) {
                XHttpsReceiver httpsReceiver = (XHttpsReceiver) receiver;
                httpsReceiver.setTlsHandler(getTlsHandler());
                httpsReceiver.setDataReceiver(this);
            } else {
                setReceive(new XHttpsReceiver(getTlsHandler(), this));
            }
        } else {
            if (sender == null || sender instanceof XHttpsSender) {
                setSender(new NioSender(selectionKey, channel));
            } else {
                sender.setChannel(selectionKey, channel);
            }
            if (receiver == null || receiver instanceof XHttpsReceiver) {
                setReceive(new XHttpReceiver(this));
            } else {
                receiver.setDataReceiver(this);
            }
        }
        byte[] head = httpProtocol.toByte();
        getSender().setSenderFeedback(this);
        getSender().sendData(head);
        getSender().sendData(request.getSendData());
//        LogDog.d("==> head = " + new String(head));
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
    }
}
