package com.currency.net.xhttp;

import com.currency.net.base.joggle.*;
import com.currency.net.nio.NioClientTask;
import com.currency.net.nio.NioReceiver;
import com.currency.net.nio.NioSender;
import com.currency.net.xhttp.config.XHttpConfig;
import com.currency.net.xhttp.entity.XHttpDecoderStatus;
import com.currency.net.xhttp.entity.XRequest;
import com.currency.net.xhttp.entity.XResponse;
import com.currency.net.xhttp.joggle.IXHttpDns;
import com.currency.net.xhttp.joggle.IXHttpResponseConvert;
import com.currency.net.xhttp.utils.*;
import log.LogDog;
import util.StringEnvoy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class XNioHttpTask extends NioClientTask implements ISenderFeedback, INetReceiver<ReuseDirectBuf> {

    private XRequest mRequest;
    private XHttpConfig mHttpConfig;
    private INetTaskContainer<NioClientTask> mNetTaskFactory;
    private XHttpProtocol mHttpProtocol;
    private XHttpDecoderProcessor mHttpDecoderProcessor;

    private boolean mIsRedirect = false;


    XNioHttpTask(INetTaskContainer<NioClientTask> taskFactory, XHttpConfig httpConfig, XRequest request) {
        if (request == null) {
            throw new NullPointerException("request is null !!!");
        }
        this.mHttpConfig = httpConfig;
        this.mNetTaskFactory = taskFactory;

        mHttpProtocol = new XHttpProtocol();
        initTask(request);
    }

    protected void initTask(XRequest request) {
        this.mRequest = request;
        mHttpProtocol.initProtocol(request);
        XUrlMedia httpUrlMedia = request.getUrl();
        String host = httpUrlMedia.getHost();
        IXHttpDns httpDns = mHttpConfig.getXHttpDns();
        if (httpDns != null) {
            String ip = httpDns.getCacheDns(httpUrlMedia.getHost());
            if (StringEnvoy.isNotEmpty(ip)) {
                host = ip;
            }
        }
        setAddress(host, httpUrlMedia.getPort());
        setTLS(httpUrlMedia.isTSL());
        mHttpDecoderProcessor = new XHttpDecoderProcessor();
    }

    @Override
    public void onSenderFeedBack(INetSender sender, Object data, Throwable e) {
        if (e != null) {
            e.printStackTrace();
            mNetTaskFactory.addUnExecTask(this);
        }
        if (data instanceof ReuseDirectBuf) {
            XMultiplexCacheManger.getInstance().lose((ReuseDirectBuf) data);
        }
    }


    @Override
    public void onReceiveFullData(ReuseDirectBuf buf, Throwable e) {
        byte[] data = buf.array();
        mHttpDecoderProcessor.decoderData(data, data.length);
        XMultiplexCacheManger.getInstance().lose(buf);
        XHttpDecoderStatus status = mHttpDecoderProcessor.getStatus();
        if (status == XHttpDecoderStatus.OVER) {
            XResponse response = mHttpDecoderProcessor.getResponse();
            int code = XResponseHelper.getCode(response);
            if (code >= 300 && code < 400) {
                //重定向
                LogDog.w("## has redirect !!!");
                String location = response.getHeadForKey(XHttpProtocol.XY_LOCATION);
                mRequest.setUrl(location);
                mIsRedirect = true;
            } else {
                IXHttpResponseConvert responseConvert = mHttpConfig.getResponseConvert();
                if (responseConvert != null) {
                    responseConvert.handlerEntity(mRequest, response);
                }
                IXSessionNotify sessionNotify = mHttpConfig.getSessionNotify();
                if (sessionNotify != null) {
                    sessionNotify.notifyData(mRequest, response, e);
                }
            }
            mHttpDecoderProcessor.reset();
            mNetTaskFactory.addUnExecTask(this);
        }
    }


    @Override
    protected void onErrorChannel(Throwable throwable) {
        IXSessionNotify sessionNotify = mHttpConfig.getSessionNotify();
        if (sessionNotify != null) {
            sessionNotify.notifyData(mRequest, null, throwable);
        }
    }

    @Override
    protected void onBeReadyChannel(SocketChannel channel) {
        XUrlMedia httpUrlMedia = mRequest.getUrl();
        IXHttpDns httpDns = mHttpConfig.getXHttpDns();
        if (httpDns != null) {
            InetSocketAddress address = null;
            try {
                address = (InetSocketAddress) channel.getRemoteAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpDns.setCacheDns(httpUrlMedia.getHost(), address.getAddress().getHostAddress());
        }
        NioReceiver receiver = getReceiver();
        NioSender sender = getSender();

        if (httpUrlMedia.isTSL()) {
            if (sender instanceof XHttpsSender) {
                XHttpsSender httpsSender = (XHttpsSender) sender;
                httpsSender.setTlsHandler(getTlsHandler());
                httpsSender.setChannel(mSelectionKey, channel);
            } else {
                setSender(new XHttpsSender(getTlsHandler(), mSelectionKey, channel));
            }
            if (receiver instanceof XHttpsReceiver) {
                //说明上次任务也是https，则复用
                XHttpsReceiver httpsReceiver = (XHttpsReceiver) receiver;
                httpsReceiver.setTlsHandler(getTlsHandler());
            } else {
                receiver = new XHttpsReceiver(getTlsHandler());
                receiver.setDataReceiver(this);
                setReceiver(receiver);
            }
        } else {
            if (sender == null || sender instanceof XHttpsSender) {
                setSender(new NioSender(mSelectionKey, channel));
            } else {
                sender.setChannel(mSelectionKey, channel);
            }
            if (receiver == null || receiver instanceof XHttpsReceiver) {
                receiver = new NioReceiver();
                receiver.setDataReceiver(this);
                setReceiver(receiver);
            }
        }

        byte[] head = mHttpProtocol.toByte();
        getSender().setSenderFeedback(this);
        getSender().sendData(head);
        getSender().sendData(mRequest.getSendData());
//        LogDog.d("==> head = " + new String(head));
    }


    @Override
    protected void onRecovery() {
        super.onRecovery();
        if (mIsRedirect) {
            //是否是重定向
            initTask(mRequest);
            mNetTaskFactory.addExecTask(this);
            mIsRedirect = false;
        } else {
            //移除任务记录
            XMultiplexCacheManger.getInstance().lose(this);
        }
    }
}
