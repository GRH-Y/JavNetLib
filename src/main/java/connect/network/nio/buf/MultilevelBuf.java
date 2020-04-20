package connect.network.nio.buf;

import util.DirectBufferCleaner;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 多级直接字节缓存
 */
public class MultilevelBuf {

    //当前可用的buf在集合的索引
    private volatile int bufIndex = 0;
    //当前可用buf指针位置
    private volatile int offset = 0;
    //标记potions的备份
    private volatile int mark = -1;
    //当前数据占缓存的容量
    private volatile int limit;
    //当前缓存最大的容量
    private volatile int capacity;
    //buf集合
    private final List<ByteBuffer> bufList;
    //默认每个buf的大小
    private final int initSize;
    private final int DEFAULT_SIZE = 8096;
    //是否是借用状态
    private boolean isLendStatus = false;

    public MultilevelBuf() {
        bufList = new LinkedList<>();
        this.initSize = DEFAULT_SIZE;
        appendBuffer();
    }

    public MultilevelBuf(int initSize) {
        bufList = new ArrayList<>();
        this.initSize = initSize;
        appendBuffer();
    }


//    public List<ByteBuffer> getBufList() {
//        return bufList;
//    }

    private void appendBuffer() {
        bufList.add(ByteBuffer.allocate(initSize));
        capacity += initSize;
        limit += initSize;
//        LogDog.d("bufList size = " + bufList.size());
    }

    public final ByteBuffer[] getAllBuf() {
        synchronized (bufList) {
            int size = bufIndex + (offset > 0 ? 1 : 0);
            ByteBuffer[] buffers = new ByteBuffer[size];
            for (int index = 0; index < buffers.length; index++) {
                buffers[index] = bufList.get(index);
            }
            return buffers;
        }
    }


    public final ByteBuffer getLendBuf() {
        synchronized (bufList) {
            if (isLendStatus) {
                return null;
            }
            isLendStatus = true;
            return bufList.get(bufIndex);
        }
    }

    public final void setBackBuf(ByteBuffer buffer) {
        synchronized (bufList) {
            if (!isLendStatus || buffer == null) {
                return;
            }
            if (bufList.get(bufIndex) == buffer) {
                if (buffer.position() == buffer.capacity()) {
                    appendBuffer();
                    bufIndex++;
                    offset = 0;
                } else {
                    offset = buffer.position();
                }
                isLendStatus = false;
            }
        }
    }

    public final byte[] array() {
        synchronized (bufList) {
            if (isLendStatus) {
                throw new IllegalStateException("currently in borrowing state,please call setBackBuf() !!!");
            }
            if (limit <= 0) {
                return null;
            }
            byte[] data = new byte[limit];
            int sumOffset = 0;

            for (int index = 0; index <= bufIndex; index++) {
                ByteBuffer buffer = bufList.get(index);
                int length = buffer.position();
                buffer.flip();
                buffer.get(data, sumOffset, length);
                //恢复 limit
                buffer.limit(initSize);
                //恢复 position
                buffer.position(length);
                sumOffset += length;
            }
            return data;
        }
    }

//    public final int limit() {
//        synchronized (bufList) {
//            return limit;
//        }
//    }
//
//    public final void limit(int newLimit) {
//        if ((newLimit > capacity) || (newLimit < 0)) {
//            throw new IllegalArgumentException();
//        }
//        limit = newLimit;
//        if (position() > limit) {
//            position(limit);
//        }
//    }
//
//    private final void position(int position) {
//        if (position <= 0) {
//            return;
//        }
//        synchronized (bufList) {
//            bufIndex = position / initSize;
//            offset = position % initSize;
//        }
//    }

    private final int position() {
        synchronized (bufList) {
            return bufIndex * initSize + offset;
        }
    }

    public final int capacity() {
        synchronized (bufList) {
            return capacity;
        }
    }

    public final int hasRemaining() {
        synchronized (bufList) {
            return limit - position();
        }
    }

//    public final void mark() {
//        synchronized (bufList) {
//            mark = position();
//        }
//    }
//
//    public final int getMark() {
//        synchronized (bufList) {
//            return mark;
//        }
//    }
//
//    public final void reset() {
//        synchronized (bufList) {
//            position(mark);
//            mark = -1;
//        }
//    }

    public final void flip() {
        synchronized (bufList) {
            limit = position();
//            bufIndex = 0;
//            offset = 0;
        }
    }

//    public final void rewind() {
//        bufIndex = 0;
//        offset = 0;
//        mark = -1;
//    }

    public final void clear() {
        synchronized (bufList) {
            for (ByteBuffer buffer : bufList) {
                buffer.clear();
            }
            limit = capacity;
            bufIndex = 0;
            offset = 0;
            mark = -1;
        }
    }

    public final void release() {
        synchronized (bufList) {
            clear();
            for (ByteBuffer buffer : bufList) {
                DirectBufferCleaner.clean(buffer);
            }
        }
    }

    @Override
    public String toString() {
        return "MultilevelBuf[" +
                "bufIndex=" + bufIndex +
                ", offset=" + offset +
                ", mark=" + mark +
                ", limit=" + limit +
                ", capacity=" + capacity +
                ", bufList=" + bufList +
                ", initSize=" + initSize +
                ", DEFAULT_SIZE=" + DEFAULT_SIZE +
                ", isLendStatus=" + isLendStatus +
                ']';
    }
}
