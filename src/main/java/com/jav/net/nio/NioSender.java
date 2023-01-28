package com.jav.net.nio;


import com.jav.net.entity.MultiByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Nio客户端的数据发送者
 *
 * @author yyz
 */
public class NioSender extends AbsNioCacheNetSender<MultiByteBuffer> {

    protected SocketChannel mChannel;

    public void setChannel(SelectionKey selectionKey, SocketChannel channel) {
        if (selectionKey == null || channel == null) {
            throw new NullPointerException("selectionKey or channel is null !!!");
        }
        this.mSelectionKey = selectionKey;
        this.mChannel = channel;
    }

    @Override
    public void sendData(MultiByteBuffer data) {
        if (data == null || data.isClear()) {
            // 当前buf没有内容
            return;
        }
        super.sendData(data);
    }

    @Override
    protected int sendDataImp(ByteBuffer[] buffers) throws IOException {
        if (mChannel == null || buffers == null || !mChannel.isConnected()) {
            return SEND_FAIL;
        }
        do {
            long ret = mChannel.write(buffers);
            if (ret < 0) {
                return SEND_FAIL;
            } else if (ret == 0) {
                return SEND_CHANNEL_BUSY;
            }
        } while (hasRemaining(buffers) && mChannel.isConnected());
        return SEND_COMPLETE;
    }
}
