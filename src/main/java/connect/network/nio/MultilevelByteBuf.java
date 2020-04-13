package connect.network.nio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MultilevelByteBuf {
    private volatile int bufIndex = 0;
    private volatile int index = 0;
    private List<ByteBuffer> bufList;

    public MultilevelByteBuf() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
//        byteBuffer.put();
        bufList = new ArrayList<>();
        bufList.add(ByteBuffer.allocateDirect(1024));
    }

//    public List<ByteBuffer> getBufList() {
//        return bufList;
//    }

    public ByteBuffer lendBuf() {
        synchronized (bufList) {
            return bufList.get(bufIndex);
        }
    }

    public void goBackBuf(ByteBuffer buffer) {
        synchronized (bufList) {
            if (lendBuf() == buffer) {
                if (buffer.position() == buffer.capacity()) {
                    bufList.add(ByteBuffer.allocateDirect(1024));
                    bufIndex++;
                    index = 0;
                } else {
                    index = buffer.position();
                }
            }
        }
    }

//    public void position(long position) {
//        synchronized (bufList) {
//            bufIndex = (int) (position / 1024);
//            index = (int) (position % 1024);
//        }
//    }

    public long position() {
        synchronized (bufList) {
            return bufIndex * 1024 + index;
        }
    }

    public long hasRemaining() {
        synchronized (bufList) {
            return bufList.size() * 1024 - position();
        }
    }

    public void clear() {
        synchronized (bufList) {
            for (ByteBuffer buffer : bufList) {
                buffer.clear();
            }
            bufIndex = 0;
            index = 0;
        }
    }
}
