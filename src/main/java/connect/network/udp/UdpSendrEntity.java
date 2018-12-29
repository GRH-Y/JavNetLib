package connect.network.udp;

public class UdpSendrEntity {

    public int length;
    public byte[] data;

    UdpSendrEntity(byte[] data, int length) {
        this.data = data;
        this.length = length;
    }
}
