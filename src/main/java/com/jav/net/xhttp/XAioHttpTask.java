package com.jav.net.xhttp;

import com.jav.common.log.LogDog;
import com.jav.common.util.StringEnvoy;
import com.jav.net.aio.AioClientTask;
import com.jav.net.aio.AioReceiver;
import com.jav.net.aio.AioSender;
import com.jav.net.base.joggle.*;
import com.jav.net.entity.NetTaskStatusCode;
import com.jav.net.xhttp.config.XHttpConfig;
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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class XAioHttpTask extends AioClientTask implements ISenderFeedback, IAioNetReceiver {

    private XRequest mRequest;
    private final XHttpConfig mHttpConfig;
    private final INetTaskContainer<AioClientTask> mNetTaskFactory;
    private final XHttpProtocol mHttpProtocol;
    private XHttpDecoderProcessor mHttpDecoderProcessor;

    private boolean mIsRedirect = false;
    private boolean mIsComplete = false;


    XAioHttpTask(INetTaskContainer<AioClientTask> taskFactory, XHttpConfig httpConfig, XRequest request) {
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
        mIsComplete = false;
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
            mNetTaskFactory.addUnExecTask(this);
        }
    }


    @Override
    protected void onBeReadyChannel(AsynchronousSocketChannel channel) {
        XUrlMedia httpUrlMedia = mRequest.getUrl();
        IXHttpDns httpDns = mHttpConfig.getXHttpDns();
        if (httpDns != null) {
            try {
                InetSocketAddress address = (InetSocketAddress) getChannel().getRemoteAddress();
                httpDns.setCacheDns(httpUrlMedia.getHost(), address.getAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        AioReceiver receiver = getReceiver();
        AioSender sender = getSender();
        if (sender == null) {
            sender = new AioSender(getChannel());
            setSender(sender);
        } else {
            sender.setChannel(getChannel());
        }
        if (receiver == null) {
            receiver = new AioReceiver(getChannel());
            receiver.setDataReceiver(this);
            setReceiver(receiver);
        } else {
            receiver.setChannel(getChannel());
        }

        if (httpUrlMedia.isTSL()) {
            sender.setTlsHandler(getTlsHandler());
            receiver.setTlsHandler(getTlsHandler());
        }

        receiver.triggerReceiver();
        byte[] head = mHttpProtocol.toByte();
        sender.setSenderFeedback(this);
        sender.sendData(head);
        sender.sendData(mRequest.getSendData());
//        LogDog.d("==> head = " + new String(head));
    }

    @Override
    protected void onCloseChannel() {
        try {
            InetSocketAddress address = (InetSocketAddress) getChannel().getRemoteAddress();
            LogDog.d("==> XAioHttpTask onCloseClientChannel host = " + address.getHostName());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public boolean onCompleted(Integer result, ByteBuffer byteBuffer) {
        if (result == -1) {
            mNetTaskFactory.addUnExecTask(this);
        } else {
            byteBuffer.flip();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
            byteBuffer.clear();
            mHttpDecoderProcessor.decoderData(data, data.length);
            XHttpDecoderStatus status = mHttpDecoderProcessor.getStatus();
            if (status == XHttpDecoderStatus.OVER) {
                XResponse response = mHttpDecoderProcessor.getResponse();
                int code = XResponseHelper.getCode(response);
                if (code >= 300 && code < 400) {
                    //重定向
                    String location = response.getHeadForKey(XHttpProtocol.XY_LOCATION);
                    LogDog.w("## aio task has redirect host = " + location);
                    mRequest.setUrl(location);
                    mIsRedirect = true;
                } else {
                    IXHttpResponseConvert responseConvert = mHttpConfig.getResponseConvert();
                    if (responseConvert != null) {
                        responseConvert.handlerEntity(mRequest, response);
                    }
                    IXSessionNotify sessionNotify = mHttpConfig.getSessionNotify();
                    if (sessionNotify != null) {
                        sessionNotify.notifyData(mRequest, response, null);
                    }
                    mIsComplete = true;
                }
                mHttpDecoderProcessor.reset();
                mNetTaskFactory.addUnExecTask(XAioHttpTask.this);
            }
        }
        return !mIsComplete;
    }

    @Override
    public void onFailed(Throwable exc, ByteBuffer byteBuffer) {
        mNetTaskFactory.addUnExecTask(this);
        exc.printStackTrace();
//        byteBuffer.flip();
//        byte[] data = new byte[byteBuffer.remaining()];
//        byteBuffer.get(data);
//        byteBuffer.clear();
//        mHttpDecoderProcessor.onReceiveFullData(data, exc);
//        LogDog.d("==> onFailed 接收数据 = " + new String(data));
    }
}
