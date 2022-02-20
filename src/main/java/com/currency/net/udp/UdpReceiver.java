package com.currency.net.udp;

import com.currency.net.base.joggle.INetReceiver;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

public class UdpReceiver {

    protected final INetReceiver<DatagramPacket> mReceive;
    protected DatagramSocket socket = null;

    public UdpReceiver(INetReceiver<DatagramPacket> receive) {
        this.mReceive = receive;
    }

    protected void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    protected void onReadNetData() throws Exception {
        DatagramPacket receive = null;
        Exception exception = null;
        try {
            int size = socket.getReceiveBufferSize();
            byte[] buffer = new byte[size];
            receive = new DatagramPacket(buffer, buffer.length);
            socket.receive(receive);
        } catch (Exception e) {
            if (!(e instanceof SocketTimeoutException)) {
                exception = e;
            }
        } finally {
            notifyReceiver(receive, exception);
        }
        if (exception != null) {
            throw exception;
        }
    }

    protected void notifyReceiver(DatagramPacket packet, Exception exception) {
        if (mReceive != null) {
            mReceive.onReceiveFullData(packet, exception);
        }
    }
}
