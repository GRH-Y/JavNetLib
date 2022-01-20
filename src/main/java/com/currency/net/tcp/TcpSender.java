package com.currency.net.tcp;

import com.currency.net.base.AbsNetSender;

import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TcpSender extends AbsNetSender {

    protected Queue<Object> mCache;
    protected OutputStream mStream = null;

    public TcpSender() {
        mCache = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void sendData(Object objData) {
        if (objData != null) {
            mCache.add(objData);
        }
    }

//    @Override
//    public void sendDataNow(byte[] data) {
//        if (data != null) {
//            try {
//                stream.write(data);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    protected void setStream(OutputStream stream) {
        this.mStream = stream;
    }

    protected void onSendNetData() throws Throwable {
        while (!mCache.isEmpty() && mStream != null) {
            Object objData = mCache.remove();
            onHandleSendData(objData);
        }
    }

    protected int onHandleSendData(Object objData) throws Throwable {
        if (objData instanceof byte[]) {
            try {
                byte[] data = (byte[]) objData;
                mStream.write(data);
            } catch (Exception e) {
                if (!(e instanceof SocketTimeoutException)) {
                    throw new Exception(e);
                }
            }
        }
        return SEND_COMPLETE;
    }
}
