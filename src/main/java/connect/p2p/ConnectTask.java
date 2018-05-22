package connect.p2p;


import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceive;
import connect.network.nio.NioSender;
import connect.network.nio.interfaces.INioReceive;
import connect.network.nio.interfaces.INioSender;
import connect.p2p.bean.AddressBean;
import task.utils.Logcat;

public class ConnectTask extends NioClientTask {

    private NioSender sender;
    private NioReceive receive;

    public ConnectTask(AddressBean addressBean) {
        setAddress(addressBean.getIp(), addressBean.getPort());
        sender = new NioSender();
        receive = new NioReceive(this, "onReceiveData");
    }

    private void onReceiveData(byte[] data) {

    }

    @Override
    public void onConnect(boolean isConnect) {
        Logcat.i("==> InterFlowTask onConnect = " + isConnect);
        if (isConnect) {
            setting.disableWriteEvent();
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
