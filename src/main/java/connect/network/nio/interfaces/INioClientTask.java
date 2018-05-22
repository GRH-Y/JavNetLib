package connect.network.nio.interfaces;

public interface INioClientTask extends INioNetTask {

    INioSender getSender();

    INioReceive getReceive();

}
