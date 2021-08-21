package connect.network.xhttp.utils;

import connect.network.base.joggle.INetReceiver;
import connect.network.xhttp.XMultiplexCacheManger;
import connect.network.xhttp.entity.XReceiverMode;
import connect.network.xhttp.entity.XReceiverStatus;
import connect.network.xhttp.entity.XResponse;
import log.LogDog;
import util.StringEnvoy;

import java.util.Map;

public class XHttpDecoderProcessor implements INetReceiver<MultiLevelBuf> {

    /**
     * 接收体（结果）
     */
    protected XResponse mResponse;

    /**
     * 当前模式
     */
    private XReceiverMode mMode = XReceiverMode.RESPONSE;
    /**
     * 当前解析状态
     */
    private XReceiverStatus mStatus = XReceiverStatus.HEAD;

    /**
     * body数据是否是分段传输
     */
    private boolean mIsSubsection = false;
    /**
     * body数据大小
     */
    private int mBodySize = -1;
    /**
     * 协议头分隔符的位置
     */
    private int mHeadEndIndex;

    private INetReceiver<XResponse> mDataReceiver;

    public XHttpDecoderProcessor() {
        mResponse = new XResponse();
    }

    public void setDataReceiver(INetReceiver<XResponse> receiver) {
        this.mDataReceiver = receiver;
    }

    public XReceiverStatus getStatus() {
        return mStatus;
    }

    public XReceiverMode getMode() {
        return mMode;
    }

    public void setMode(XReceiverMode mode) {
        this.mMode = mode;
    }


    @Override
    public void onReceiveFullData(MultiLevelBuf buf, Throwable e) {
        byte[] data = buf.array();
        if (data != null) {
            int len = data.length;
            onHttpReceive(data, len, e);
        } else {
            LogDog.w("## http receiver data is null !!!");
        }
        XMultiplexCacheManger.getInstance().lose(buf);
    }


    protected void onHttpReceive(byte[] data, int len, Throwable e) {
        if (mMode == XReceiverMode.REQUEST) {
            onRequest(data, len, e);
        } else {
            onResponse(data, len, e);
        }
    }

    protected void onRequest(byte[] data, int len, Throwable e) {
        mResponse.appendRawData(data);
        processHttpHead(data, len);
        processRequestBody(data, len);
        processNotify(e);
    }

    protected void onResponse(byte[] data, int len, Throwable e) {
        mResponse.appendRawData(data);
        processHttpHead(data, len);
        processResponseBody(data, len);
        processNotify(e);
    }

    protected void onStatusChange(XReceiverStatus status) {
    }

    protected void processHttpHead(byte[] data, int len) {
        if (mStatus == XReceiverStatus.HEAD) {
            mHeadEndIndex = XResponseHelper.findHeadEndTag(data, len);
            if (mHeadEndIndex != -1) {
                //找到协议头结束标志，则开始解析协议头
                analysisHead(data, mHeadEndIndex);
                String length = mResponse.getHeadForKey(XHttpProtocol.XY_CONTENT_LENGTH);
                if (StringEnvoy.isNotEmpty(length)) {
                    mBodySize = Integer.parseInt(length);
                }
                String transfer = mResponse.getHeadForKey(XHttpProtocol.XY_TRANSFER_ENCODING);
                mIsSubsection = StringEnvoy.isNotEmpty(transfer);
                mStatus = XReceiverStatus.BODY;
                callStatusChange();
            }
        }
    }

    /**
     * 处理请求数据里面body数据
     *
     * @param data
     * @param len
     */
    protected void processRequestBody(byte[] data, int len) {
        if (mStatus == XReceiverStatus.BODY) {
            if (mBodySize != -1) {
                ByteCacheStream raw = mResponse.getRawData();
                if (raw.size() - mHeadEndIndex == mBodySize) {
                    byte[] body = new byte[mBodySize];
                    System.arraycopy(raw.getBuf(), mHeadEndIndex, body, 0, body.length);
                    mResponse.setHttpData(body);
                    mStatus = XReceiverStatus.OVER;
                    callStatusChange();
                }
            } else {
                //没有请求体
                int index = XResponseHelper.findHeadEndTag(data, len);
                if (index != -1) {
                    mStatus = XReceiverStatus.OVER;
                    callStatusChange();
                }
            }
        }
    }

    /**
     * 处理接收数据里面的body数据
     *
     * @param data
     * @param len
     */
    protected void processResponseBody(byte[] data, int len) {
        if (mStatus == XReceiverStatus.BODY) {
            ByteCacheStream raw = mResponse.getRawData();
            byte[] body = null;
            //分段传输方式
            if (mIsSubsection) {
                //查找0\r\n结束标志
                int index = XResponseHelper.findBodyEndTag(data, len);
                if (index != -1) {
                    body = new byte[raw.size() - 5 - mHeadEndIndex];
                }
            } else {
                //有明确的数据大小
                if (raw.size() - mHeadEndIndex == mBodySize) {
                    body = new byte[mBodySize];
                }
            }
            if (body != null) {
                System.arraycopy(raw.getBuf(), mHeadEndIndex, body, 0, body.length);
                mResponse.setHttpData(body);
                mStatus = XReceiverStatus.OVER;
                callStatusChange();
            }
        }
    }

    private void processNotify(Throwable e) {
        if (mStatus == XReceiverStatus.OVER) {
            if (mDataReceiver != null) {
                mDataReceiver.onReceiveFullData(mResponse, e);
            }
            mStatus = XReceiverStatus.NONE;
            callStatusChange();
            reset();
        }
    }


    private void reset() {
        mHeadEndIndex = -1;
        mStatus = XReceiverStatus.HEAD;
        mResponse.reset();
    }

    private void callStatusChange() {
        try {
            onStatusChange(mStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void analysisHead(byte[] data, int index) {
        Map<String, String> headMap = mResponse.getHttpHead();
        String headStr = new String(data, 0, index);
        String[] arrays = headStr.split("\r\n");
        //第一行不是键值对
        headMap.put(XHttpProtocol.XY_FIST_LINE, arrays[0]);
//        LogDog.d("============== head start =====================");
        for (int tmp = 1; tmp < arrays.length; tmp++) {
            String[] kv = arrays[tmp].split(": ");
            if (kv.length == 2) {
                headMap.put(kv[0], kv[1]);
//                LogDog.d(kv[0] + ":" + kv[1]);
            }
        }
//        LogDog.d("=============== head end ====================\r\n");
    }

}
