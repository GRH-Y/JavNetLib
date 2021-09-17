package connect.network.xhttp;

import connect.network.aio.AioClientTask;
import connect.network.aio.AioReceiver;
import connect.network.aio.AioSender;
import connect.network.base.AbsNetFactory;
import connect.network.base.joggle.IAioNetReceiver;
import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;
import connect.network.base.joggle.IXSessionNotify;
import connect.network.xhttp.config.XHttpConfig;
import connect.network.xhttp.entity.XHttpDecoderStatus;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;
import connect.network.xhttp.joggle.IXHttpDns;
import connect.network.xhttp.joggle.IXHttpResponseConvert;
import connect.network.xhttp.utils.*;
import log.LogDog;
import util.StringEnvoy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class XAioHttpTask extends AioClientTask implements ISenderFeedback, IAioNetReceiver {

    private XRequest mRequest;
    private XHttpConfig mHttpConfig;
    private AbsNetFactory mNetFactory;
    private XHttpProtocol mHttpProtocol;
    private XHttpDecoderProcessor mHttpDecoderProcessor;

    private boolean mIsRedirect = false;
    private boolean mIsComplete = false;


    XAioHttpTask(AbsNetFactory netFactory, XHttpConfig httpConfig, XRequest request) {
        if (request == null) {
            throw new NullPointerException("request is null !!!");
        }
        this.mHttpConfig = httpConfig;
        this.mNetFactory = netFactory;

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
            mNetFactory.removeTask(this);
        }
        if (data instanceof MultiLevelBuf) {
            XMultiplexCacheManger.getInstance().lose((MultiLevelBuf) data);
        }
    }


    @Override
    protected void onConnectCompleteChannel() {
        XUrlMedia httpUrlMedia = mRequest.getUrl();
        IXHttpDns httpDns = mHttpConfig.getXHttpDns();
        if (httpDns != null) {
            InetSocketAddress address = null;
            try {
                address = (InetSocketAddress) getChannel().getRemoteAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpDns.setCacheDns(httpUrlMedia.getHost(), address.getAddress().getHostAddress());
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
    protected void onCloseClientChannel() {
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
            //是否是重定向
            initTask(mRequest);
            mNetFactory.addTask(this);
            mIsRedirect = false;
        } else {
            //移除任务记录
            XMultiplexCacheManger.getInstance().lose(this);
        }
    }

    @Override
    public boolean onCompleted(Integer result, ByteBuffer byteBuffer) {
        if (result.intValue() == -1) {
            mNetFactory.removeTask(this);
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
                mNetFactory.removeTask(XAioHttpTask.this);
            }
        }
        return !mIsComplete;
    }

    @Override
    public void onFailed(Throwable exc, ByteBuffer byteBuffer) {
        mNetFactory.removeTask(this);
        exc.printStackTrace();
//        byteBuffer.flip();
//        byte[] data = new byte[byteBuffer.remaining()];
//        byteBuffer.get(data);
//        byteBuffer.clear();
//        mHttpDecoderProcessor.onReceiveFullData(data, exc);
//        LogDog.d("==> onFailed 接收数据 = " + new String(data));
    }
}
