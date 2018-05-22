package connect.network.nio.interfaces;


import java.nio.channels.spi.AbstractSelectableChannel;

public interface INioNetTask {

    AbstractSelectableChannel getSocketChannel();

//    SocketAddress getSocketAddress();

    void onConnect(boolean isConnect);

    void onClose();
}
