package com.currency.net.xhttp;

import com.currency.net.base.SendPacket;
import com.currency.net.base.joggle.*;
import com.currency.net.entity.MultiByteBuffer;
import com.currency.net.entity.NetTaskStatusCode;
import com.currency.net.nio.NioClientTask;
import com.currency.net.nio.NioReceiver;
import com.currency.net.nio.NioSender;
import com.currency.net.xhttp.config.XHttpConfig;
import com.currency.net.xhttp.entity.XHttpDecoderStatus;
import com.currency.net.xhttp.entity.XRequest;
import com.currency.net.xhttp.entity.XResponse;
import com.currency.net.xhttp.joggle.IXHttpDns;
import com.currency.net.xhttp.joggle.IXHttpResponseConvert;
import com.currency.net.xhttp.utils.XHttpDecoderProcessor;
import com.currency.net.xhttp.utils.XHttpProtocol;
import com.currency.net.xhttp.utils.XResponseHelper;
import com.currency.net.xhttp.utils.XUrlMedia;
import log.LogDog;
import util.StringEnvoy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class XNioHttpTask extends NioClientTask implements ISenderFeedback, INetReceiver<MultiByteBuffer> {

    private XRequest mRequest;
    private final XHttpConfig mHttpConfig;
    private final INetTaskContainer<NioClientTask> mNetTaskFactory;
    private final XHttpProtocol mHttpProtocol;
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
    }


    @Override
    public void onReceiveFullData(MultiByteBuffer buf, Throwable e) {
        byte[] data = buf.array();
        XHttpDecoderStatus status = XHttpDecoderStatus.OVER;
        if (data != null) {
            mHttpDecoderProcessor.decoderData(data, data.length);
            status = mHttpDecoderProcessor.getStatus();
        }
        getReceiver().reuseBuf(buf);
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
        getSender().sendData(SendPacket.getInstance(head));
        getSender().sendData(SendPacket.getInstance(mRequest.getSendData()));
//        LogDog.d("==> head = " + new String(head));
    }


    @Override
    protected void onRecovery() {
        super.onRecovery();
        if (mIsRedirect) {
            //是不是重定向
            initTask(mRequest);
            mNetTaskFactory.addExecTask(this);
            mIsRedirect = false;
        } else {
            //复用task
            setTaskStatus(NetTaskStatusCode.NONE);
            XMultiplexCacheManger.getInstance().lose(this);
        }
    }
}
