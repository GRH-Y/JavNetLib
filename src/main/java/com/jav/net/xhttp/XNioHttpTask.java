package com.jav.net.xhttp;

import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.common.util.StringEnvoy;
import com.jav.net.base.MultiBuffer;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.*;
import com.jav.net.nio.NioClientTask;
import com.jav.net.nio.NioReceiver;
import com.jav.net.nio.NioSender;
import com.jav.net.ssl.SSLComponent;
import com.jav.net.ssl.TLSHandler;
import com.jav.net.xhttp.config.XHttpConfig;
import com.jav.net.xhttp.entity.XHttpDecoderStatus;
import com.jav.net.xhttp.entity.XRequest;
import com.jav.net.xhttp.entity.XResponse;
import com.jav.net.xhttp.joggle.IXHttpDns;
import com.jav.net.xhttp.joggle.IXHttpResponseConvert;
import com.jav.net.xhttp.utils.XHttpDecoderProcessor;
import com.jav.net.xhttp.utils.XHttpProtocol;
import com.jav.net.xhttp.utils.XUrlMedia;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class XNioHttpTask extends NioClientTask implements ISenderFeedback, INetReceiver<MultiBuffer> {

    private XRequest mRequest;

    private final XHttpConfig mHttpConfig;

    private final INetTaskComponent<NioClientTask> mNetTaskFactory;

    private final XHttpProtocol mHttpProtocol;

    private XHttpDecoderProcessor mHttpDecoderProcessor;

    private TLSHandler mTLSHandler;


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
        mHttpDecoderProcessor = new XHttpDecoderProcessor();

        IControlStateMachine stateMachine = getStatusMachine();
        stateMachine.updateState(NetTaskStatus.INVALID, NetTaskStatus.NONE);
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
            IXHttpResponseConvert responseConvert = mHttpConfig.getResponseConvert();
            if (responseConvert != null) {
                responseConvert.handlerEntity(mRequest, response);
            }
            IXSessionNotify sessionNotify = mHttpConfig.getSessionNotify();
            if (sessionNotify != null) {
                sessionNotify.notifySuccess(mRequest, response);
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

    private void initSSLContext(SocketChannel channel) {
        SSLComponent sslComponent = new SSLComponent();
        mTLSHandler = TLSHandler.createSSLEngineForClient(sslComponent.getSSLContext(), getHost(), getPort());
        try {
            mTLSHandler.beginHandshakeForClient();
            mTLSHandler.doHandshake(channel);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onBeReadyChannel(SelectionKey selectionKey, SocketChannel channel) {
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
            initSSLContext(channel);
            if (sender instanceof XHttpsSender) {
                XHttpsSender httpsSender = (XHttpsSender) sender;
                httpsSender.setTlsHandler(mTLSHandler);
                httpsSender.setChannel(selectionKey, channel);
            } else {
                setSender(new XHttpsSender(mTLSHandler, selectionKey, channel));
            }
            if (receiver instanceof XHttpsReceiver) {
                XHttpsReceiver httpsReceiver = (XHttpsReceiver) receiver;
                // 说明上次任务也是https，则复用
                httpsReceiver.setTlsHandler(mTLSHandler);
            } else {
                receiver = new XHttpsReceiver(mTLSHandler);
                receiver.setDataReceiver(this);
                setReceiver(receiver);
            }
        } else {
            if (sender == null || sender instanceof XHttpsSender) {
                sender = new NioSender();
                sender.setChannel(selectionKey, channel);
                setSender(sender);
            } else {
                sender.setChannel(selectionKey, channel);
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
        if (mTLSHandler != null) {
            mTLSHandler.release();
        }
    }
}
