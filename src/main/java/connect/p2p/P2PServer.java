package connect.p2p;


import connect.json.JsonUtils;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioSender;
import connect.network.nio.NioServerTask;
import connect.network.nio.NioSocketFactory;
import connect.network.nio.interfaces.INioReceive;
import connect.p2p.bean.AddressBean;
import connect.p2p.bean.KeyBean;
import task.utils.IoUtils;
import task.utils.Logcat;
import task.utils.NetUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * P2PServer p2p服务端
 * Created by prolog on 10/25/2016.
 */

public class P2PServer extends NioServerTask {

    private Map<String, NioClientTask> nioClientTaskMap;

    public P2PServer(int port) throws SocketException {
        this("wlan", port);
    }

    public P2PServer(String netInterface, int port) throws SocketException {
        setAddress(NetUtils.getLocalIp(netInterface), port);
        nioClientTaskMap = new ConcurrentHashMap<>();
    }


    @Override
    public void onAccept(SocketChannel channel) {
        Logcat.i("has client connect P2PServer ======>");
        try {
            Client client = new Client(channel);
            NioSocketFactory socketFactory = NioSocketFactory.getFactory();
            socketFactory.open();
            socketFactory.addNioTask(client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnect(boolean isConnect) {
        Logcat.i("==> P2PServer onConnect = " + isConnect);
        Logcat.d("==> P2PServer address = " + address.toString());
    }


    private class Client extends NioClientTask {

        private NioSender sender;
        private ClientReceive receive;

        public Client(SocketChannel channel) {
            this.channel = channel;
            sender = new NioSender();
            receive = new ClientReceive();
        }

        @Override
        public void onConnect(boolean isConnect) {
            Logcat.d("==> NioServer Client onConnect = " + isConnect);
        }

        @Override
        public INioReceive getReceive() {
            return receive;
        }

        @Override
        public NioSender getSender() {
            return sender;
        }


        private class ClientReceive implements INioReceive {

            private void sendAddressInfo(SocketChannel channel, NioSender sender) {
                try {
                    AddressBean addressBean = new AddressBean();
                    InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
                    addressBean.setIp(address.getHostName());
                    addressBean.setPort(address.getPort());
                    sender.sendData(JsonUtils.toNewJson(addressBean).getBytes());
                    if (setting != null) {
                        setting.enableReadWriteEvent();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void read(SocketChannel channel) {
                byte[] data = IoUtils.tryRead(channel);
                if (data != null) {
                    Logcat.d("==> ClientReceive data = " + new String(data));
                    KeyBean keyBean = JsonUtils.toEntity(KeyBean.class, data);
                    if (nioClientTaskMap.containsKey(keyBean.getKey())) {
                        NioClientTask task = nioClientTaskMap.get(keyBean.getKey());
                        NioSender otherSender = (NioSender) task.getSender();
                        sendAddressInfo(channel, otherSender);

                        SocketChannel otherChannel = (SocketChannel) task.getSocketChannel();
                        sendAddressInfo(otherChannel, sender);
                    } else {
                        nioClientTaskMap.put(keyBean.getKey(), Client.this);
                    }
                }
            }
        }

    }
}
