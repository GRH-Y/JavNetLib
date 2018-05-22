package connect.network.nio.interfaces;

import java.nio.channels.SocketChannel;

public interface INioServerTask extends INioNetTask {

    void onAccept(SocketChannel channel);

}
