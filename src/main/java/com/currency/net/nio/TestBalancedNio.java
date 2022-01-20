package com.currency.net.nio;

import com.currency.net.base.NetTaskComponent;
import com.currency.net.base.joggle.INetReceiver;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.ssl.TLSHandler;
import com.currency.net.xhttp.XHttpsReceiver;
import com.currency.net.xhttp.XHttpsSender;
import com.currency.net.xhttp.utils.ReuseDirectBuf;
import log.LogDog;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TestBalancedNio {

    private static AtomicInteger sCount = new AtomicInteger(0);

    private static class NioBalancedClient extends NioClientTask implements INetReceiver<ReuseDirectBuf> {

        public NioBalancedClient() {
            setAddress("www.baidu.com", 443);
            setTLS(true);
        }

        @Override
        protected void onBeReadyChannel(SocketChannel channel) {
            XHttpsReceiver receiver = new XHttpsReceiver(getTlsHandler());
            receiver.setDataReceiver(this);
            setReceiver(receiver);
            XHttpsSender sender = new XHttpsSender(getTlsHandler(), mSelectionKey, channel);
            setSender(sender);
            sendBaidu();
        }

        @Override
        protected void onCreateSSLContext(ISSLFactory sslFactory) {
            mTLSHandler = TLSHandler.createSSLEngineForClient(sslFactory.getSSLContext(), getHost(), getPort());
            try {
                mTLSHandler.beginHandshakeForClient();
                mTLSHandler.doHandshake(getChannel());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }


        @Override
        protected void onErrorChannel(Throwable throwable) {
            super.onErrorChannel(throwable);
            throwable.printStackTrace();
        }

        @Override
        protected void onCloseChannel() {
            LogDog.e("--> onCloseChannel connect count = " + sCount.decrementAndGet());
        }

        @Override
        public void onReceiveFullData(ReuseDirectBuf buf, Throwable e) {
            byte[] data = buf.array();
            if (data != null) {
                LogDog.d("--> receive data = " + new String(data, 0, 15));
            }
        }


        private void sendBaidu() {
            StringBuilder builder = new StringBuilder();
            builder.append("GET / HTTP/1.1\r\n");
            builder.append("Host: www.baidu.com\r\n");
            builder.append("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:95.0) Gecko/20100101 Firefox/95.0\r\n");
            builder.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8\r\n");
            builder.append("Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2\r\n");
            builder.append("Accept-Encoding: gzip, deflate, br\r\n");
            builder.append("Connection: keep-alive\r\n");
            builder.append("\r\n");
            getSender().sendData(builder.toString().getBytes());
        }
    }

    public static void main(String[] args) {
        INetTaskContainer netTaskContainer = NioBalancedClientFactory.getFactory().open();
        int count = 10;
        sCount.set(count);
        ArrayList<NioBalancedClient> list = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            NioBalancedClient client = new NioBalancedClient();
            netTaskContainer.addExecTask(client);
            list.add(client);
        }

//        try {
//            Thread.sleep(50000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        for (int index = count - 1; index >= 0; index--) {
            if (index <= 7 && index > 5) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (index < 5) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            NioBalancedClient client = list.get(index);
            int retCode = netTaskContainer.addUnExecTask(client);
            if (retCode == NetTaskComponent.UN_EXEC_SUCCESS) {
                LogDog.i("--> connect count = " + sCount.decrementAndGet());
            }

//            LogDog.d("--> ---------------------- main start UnExecTask !!! ---------------------------------");
        }

        while (sCount.get() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        NioBalancedClientFactory.destroy();
        LogDog.i("-->  stop ing !!! ");
    }
}
