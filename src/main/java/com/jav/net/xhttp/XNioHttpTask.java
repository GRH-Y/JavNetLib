package com.jav.net.xhttp;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.common.util.StringEnvoy;
import com.jav.net.base.MultiBuffer;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.*;
import com.jav.net.nio.NioClientTask;
import com.jav.net.nio.NioReceiver;
import com.jav.net.nio.NioSender;
import com.jav.net.ssl.TLSHandler;
import com.jav.net.xhttp.config.XHttpConfig;
import com.jav.net.xhttp.entity.XHttpCode;
import com.jav.net.xhttp.entity.XHttpDecoderStatus;
import com.jav.net.xhttp.entity.XRequest;
import com.jav.net.xhttp.entity.XResponse;
import com.jav.net.xhttp.joggle.IXHttpDns;
import com.jav.net.xhttp.joggle.IXHttpResponseConvert;
import com.jav.net.xhttp.utils.XHttpDecoderProcessor;
import com.jav.net.xhttp.utils.XHttpProtocol;
import com.jav.net.xhttp.utils.XResponseHelper;
import com.jav.net.xhttp.utils.XUrlMedia;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class XNioHttpTask extends NioClientTask implements ISenderFeedback, INetReceiver<MultiBuffer> {

    private XRequest mRequest;
    private final XHttpConfig mHttpConfig;
    private final INetTaskComponent<NioClientTask> mNetTaskFactory;
    private final XHttpProtocol mHttpProtocol;
    private XHttpDecoderProcessor mHttpDecoderProcessor;

    private boolean mIsRedirect = false;


    XNioHttpTask(INetTaskComponent<NioClientTask> taskFactory, XHttpConfig httpConfig, XRequest request) {
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
        setTls(httpUrlMedia.isTSL());
        mHttpDecoderProcessor = new XHttpDecoderProcessor();
    }

    @Override
    public void onSenderFeedBack(INetSender sender, Object data, Throwable e) {
        if (e != null) {
            e.printStackTrace();
            mNetTaskFactory.addUnExecTask(this);
        }
    }


    @Override
    public void onReceiveFullData(MultiBuffer buf) {
        byte[] data = buf.asByte();
        XHttpDecoderStatus status = XHttpDecoderStatus.OVER;
        if (data != null) {
            mHttpDecoderProcessor.decoderData(data, data.length);
            status = mHttpDecoderProcessor.getStatus();
        }
        if (status == XHttpDecoderStatus.OVER) {
            XResponse response = mHttpDecoderProcessor.getResponse();
            int code = XResponseHelper.getCode(response);
            if (code >= XHttpCode.REDIRECT.getCode() && code < XHttpCode.NOT_FOUND.getCode()) {
                // 重定向
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
                    sessionNotify.notifySuccess(mRequest, response);
                }
            }
            mHttpDecoderProcessor.reset();
            mNetTaskFactory.addUnExecTask(this);
        }
    }

    @Override
    public void onReceiveError(Throwable e) {
    }

    @Override
    protected void onErrorChannel(NetErrorType errorType, Throwable throwable) {
        IXSessionNotify sessionNotify = mHttpConfig.getSessionNotify();
        if (sessionNotify != null) {
            sessionNotify.notifyError(mRequest, throwable);
        }
    }

    @Override
    protected void onCreateSSLContext(ISSLComponent sslFactory) {
        TLSHandler tlsHandler = TLSHandler.createSSLEngineForClient(sslFactory.getSSLContext(), getHost(), getPort());
        try {
            tlsHandler.beginHandshakeForClient();
            tlsHandler.doHandshake(getChannel());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        setTLSHandler(tlsHandler);
    }

    @Override
    protected void onBeReadyChannel(SocketChannel channel) {
        XUrlMedia httpUrlMedia = mRequest.getUrl();
        IXHttpDns httpDns = mHttpConfig.getXHttpDns();
        if (httpDns != null) {
            try {
                InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
                httpDns.setCacheDns(httpUrlMedia.getHost(), address.getAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                XHttpsReceiver httpsReceiver = (XHttpsReceiver) receiver;
                // 说明上次任务也是https，则复用
                httpsReceiver.setTlsHandler(getTlsHandler());
            } else {
                receiver = new XHttpsReceiver(getTlsHandler());
                receiver.setDataReceiver(this);
                setReceiver(receiver);
            }
        } else {
            if (sender == null || sender instanceof XHttpsSender) {
                sender = new NioSender();
                sender.setChannel(mSelectionKey, channel);
                setSender(sender);
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
        getSender().sendData(new MultiBuffer(head));
        byte[] body = mRequest.getSendData();
        if (body != null) {
            getSender().sendData(new MultiBuffer(body));
        }
        //        LogDog.d("==> head = " + new String(head));
    }


    @Override
    protected void onRecovery() {
        super.onRecovery();
        if (mIsRedirect) {
            // 是不是重定向
            initTask(mRequest);
            mNetTaskFactory.addExecTask(this);
            mIsRedirect = false;
        } else {
            // 复用task
            IControlStateMachine stateMachine = getStatusMachine();
            stateMachine.updateState(NetTaskStatus.INVALID, NetTaskStatus.NONE);
            XMultiplexCacheManger.getInstance().lose(this);
        }
    }
}
