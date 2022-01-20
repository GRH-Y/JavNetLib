package com.currency.net.nds;

public class DnsQueries {

    private static DnsQueries sDnsQueries;

    private final byte[] start = new byte[]{(byte) 0x5f, 0x48, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private final byte[] end = new byte[]{0x00, 0x00, 0x01, 0x00, 0x01};

    public static DnsQueries getInstance() {
        if (sDnsQueries == null) {
            synchronized (DnsQueries.class) {
                if (sDnsQueries == null) {
                    sDnsQueries = new DnsQueries();
                }
            }
        }
        return sDnsQueries;
    }

    private DnsQueries() {
    }

    public byte[] queries(String hostName) {
        String[] arrays = hostName.split("\\.");
        byte[] hostData = new byte[hostName.length() + 1];
        int destPos = 0;
        for (int index = 0; index < arrays.length; index++) {
            String str = arrays[index];
            int length = str.length();
            hostData[destPos] = (byte) length;
            destPos++;
            System.arraycopy(str.getBytes(), 0, hostData, destPos, length);
            destPos += length;
        }
        byte[] data = new byte[start.length + end.length + hostData.length];
        System.arraycopy(start, 0, data, 0, start.length);
        System.arraycopy(hostData, 0, data, start.length, hostData.length);
        System.arraycopy(end, 0, data, start.length + hostData.length, end.length);
        return data;
    }
}
