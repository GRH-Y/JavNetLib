package com.jav.net.nio;


import com.jav.common.log.LogDog;
import com.jav.net.base.MultiBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Nio客户端的数据发送者
 *
 * @author yyz
 */
public class NioSender extends AbsNioCacheNetSender<SocketChannel, MultiBuffer> {


    @Override
    public void sendData(MultiBuffer data) {
        if (data == null || data.isClear()) {
            // 当前buf没有内容
            LogDog.w("#NioSender# data is null or data is clear !");
            return;
        }
        super.sendData(data);
    }

    @Override
    protected int sendDataImp(Object data) throws IOException {
        if (mChannel == null || data == null || !mChannel.isConnected()) {
            return SEND_FAIL;
        }
        ByteBuffer[] buffers = null;
        if (data instanceof ByteBuffer[]) {
            buffers = (ByteBuffer[]) data;
        }
        if (buffers == null) {
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

    protected boolean hasRemaining(ByteBuffer[] buffers) {
        for (ByteBuffer buffer : buffers) {
            if (buffer.hasRemaining()) {
                return true;
            }
        }
        return false;
    }


}
