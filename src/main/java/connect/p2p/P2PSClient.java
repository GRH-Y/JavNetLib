package connect.p2p;


import connect.json.JsonUtils;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceive;
import connect.network.nio.NioSender;
import connect.network.nio.NioSocketFactory;
import connect.network.nio.interfaces.INioReceive;
import connect.network.nio.interfaces.INioSender;
import connect.p2p.bean.AddressBean;
import connect.p2p.bean.KeyBean;
import task.utils.Logcat;

import java.io.IOException;

/**
 * P2P客户端
 * Created by prolog on 11/23/2016.
 */

public class P2PSClient extends NioClientTask {

    private NioSender sender;
    private NioReceive receive;

    public P2PSClient(String ip, int port) {
        setAddress(ip, port);
        sender = new NioSender();
        receive = new NioReceive(this, "onReceiveData");
    }

    @Override
    public void onConnect(boolean isConnect) {
        Logcat.i("==> P2PSClient onConnect = " + isConnect);
        KeyBean keyBean = new KeyBean();
        keyBean.setKey("dhu23dh8f3c834fhn24fh919xmb3");
        String json = JsonUtils.toNewJson(keyBean);
        if (isConnect) {
            sender.setSetting(setting);
            sender.sendData(json.getBytes());
            setting.enableReadWriteEvent();
        }
    }

    private void onReceiveData(byte[] data) {
        String json = new String(data);
        Logcat.d("==> NioReceive data = " + json);
        if (json.contains("ip")) {
            AddressBean addressBean = JsonUtils.toEntity(AddressBean.class, json);
            ConnectTask task = new ConnectTask(addressBean);
            try {
                NioSocketFactory socketFactory = NioSocketFactory.getFactory();
                socketFactory.addNioTask(task);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public INioSender getSender() {
        return sender;
    }

    @Override
    public INioReceive getReceive() {
        return receive;
    }
}
