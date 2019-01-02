package connect.network.udp;

public class UdpSenderEntity {

    public int length;
    public byte[] data;

    UdpSenderEntity(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }
}
