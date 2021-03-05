package connect.network.udp;

public class UdpSenderCarrier {

    public String host;
    public int port;
    public int length;
    public byte[] data;

    public UdpSenderCarrier(String host, int port, byte[] data, int length) {
        this.host = host;
        this.port = port;
        this.data = data;
        this.length = length;
    }
}