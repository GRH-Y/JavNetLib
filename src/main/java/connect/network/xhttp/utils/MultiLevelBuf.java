package connect.network.xhttp.utils;

import log.LogDog;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 多级直接字节缓存
 */
public class MultiLevelBuf {

    //默认每个buf的大小
    private final int initSize;
    private final int DEFAULT_SIZE = 16921;

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

    //临时缓存，一般用于缓存getUseBuf没有处理完
    private ByteBuffer[] tmpCacheBuf = null;
    //buf集合
    private final List<ByteBuffer> bufList;

    //借用数量
    private int lendCount = 0;

    public MultiLevelBuf() {
        bufList = new LinkedList<>();
        this.initSize = DEFAULT_SIZE;
        appendBuffer();
    }

    public MultiLevelBuf(int initSize) {
        bufList = new ArrayList<>();
        this.initSize = initSize;
        appendBuffer();
    }


    public void appendBuffer() {
        bufList.add(ByteBuffer.allocateDirect(initSize));
        capacity += initSize;
        limit += initSize;
//        LogDog.d("bufList size = " + bufList.size());
    }

    public ByteBuffer[] getMarkBuf() {
        synchronized (bufList) {
            return tmpCacheBuf;
        }
    }


    public final ByteBuffer[] getUseBuf() {
        return getUseBuf(false);
    }

    /**
     * 获取已用的buf
     *
     * @param isFlip 为true则对每个ByteBuffer进行flip调用
     * @return
     */
    public final ByteBuffer[] getUseBuf(boolean isFlip) {
        synchronized (bufList) {
            if (lendCount > 0) {
                LogDog.e("## getUseBuf buf is use ing !!!");
                return null;
            }
            int size = bufIndex + (offset > 0 ? 1 : 0);
            ByteBuffer[] buffers = new ByteBuffer[size];
            try {
                for (int index = 0; index < buffers.length; index++) {
                    ByteBuffer tmp = bufList.get(index);
                    if (isFlip) {
                        tmp.flip();
                    }
                    buffers[index] = tmp;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            lendCount = size;
            return buffers;
        }
    }

    /**
     * 获取所有的buf
     *
     * @return
     */
    public final ByteBuffer[] getAllBuf() {
        synchronized (bufList) {
            if (lendCount > 0) {
                LogDog.e("## getAllBuf buf is use ing !!!");
                return null;
            }
            ByteBuffer[] buffers = new ByteBuffer[bufList.size()];
            lendCount = buffers.length;
            return bufList.toArray(buffers);
        }
    }


    /**
     * 租用buf（如果已租用buf则返回null）
     *
     * @return
     */
    public final ByteBuffer getLendBuf() {
        synchronized (bufList) {
            if (lendCount > 0) {
                LogDog.e("## getLendBuf buf is use ing !!!");
                return null;
            }
            lendCount = 1;
            return bufList.get(bufIndex);
        }
    }

    public final void markBuf(ByteBuffer... buffer) {
        synchronized (bufList) {
            this.tmpCacheBuf = buffer;
        }
    }


    /**
     * 归还buf
     *
     * @param buffer
     */
    public final void setBackBuf(ByteBuffer... buffer) {
        synchronized (bufList) {
            if (lendCount == 0 || buffer == null || buffer.length == 0 || lendCount != buffer.length) {
                return;
            }
            for (ByteBuffer tmp : buffer) {
                if (!bufList.contains(tmp)) {
                    //发现不存在的buf
                    LogDog.e("## Found non-existent buf");
                    return;
                }
            }
            lendCount = 0;
            for (int index = 0; index < buffer.length; index++) {
                if (buffer[index].hasRemaining()) {
                    //buf没有存满则认为最后的buf
                    offset = buffer[index].position();
                    bufIndex = index;
                    break;
                } else {
                    if (index == buffer.length - 1) {
                        //最后的buf存满，则扩容
                        appendBuffer();
                        bufIndex++;
                        offset = 0;
                    }
                }
            }
        }
    }

    /**
     * 把所有的buf数据装换成byte[]
     *
     * @return 把数据转换成byte数据返回
     */
    public final byte[] array() {
        synchronized (bufList) {
            if (lendCount > 0) {
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

    /**
     * 当前指针位置（多个缓存buf组合）
     *
     * @return
     */
    private final int position() {
        synchronized (bufList) {
            return bufIndex * initSize + offset;
        }
    }

    /**
     * 最大的缓存容量
     *
     * @return
     */
    public final int capacity() {
        synchronized (bufList) {
            return capacity;
        }
    }

    /**
     * 缓存是否还有空间可以存储数据
     *
     * @return true 还有空间
     */
    public final boolean hasRemaining() {
        synchronized (bufList) {
            return position() < capacity;
        }
    }


    public final boolean isHasData() {
        synchronized (bufList) {
            return position() > 0;
        }
    }

    /**
     * 反转为读模式（数据大小position的值）
     */
    public final void flip() {
        synchronized (bufList) {
            limit = position();
//            bufIndex = 0;
//            offset = 0;
        }
    }


    /**
     * 清除所有的标记
     */
    public final void clear() {
        synchronized (bufList) {
            for (ByteBuffer buffer : bufList) {
                buffer.clear();
            }
            tmpCacheBuf = null;
            limit = capacity;
            bufIndex = 0;
            offset = 0;
            mark = -1;
        }
    }

    /**
     * 释放资源（由于是使用直接字节buf）
     */
    public final void release() {
        synchronized (bufList) {
            clear();
            bufList.clear();
//            for (ByteBuffer buffer : bufList) {
//                DirectBufferCleaner.clean(buffer);
//            }
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
                ", lendCount=" + lendCount +
                ']';
    }
}
