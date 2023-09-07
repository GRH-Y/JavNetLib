package com.jav.net.base;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 多级直接字节缓存
 *
 * @author yyz
 */
public class MultiBuffer {

    private boolean mIsDirect;

    /**
     * 默认每个buf的大小
     */
    private final int mBufSize;
    public static final int DEFAULT_SIZE = 16921;

    /**
     * 当前可用的buf在集合的索引
     */
    private volatile int mBufIndex = 0;
    /**
     * 当前可用buf指针位置
     */
    private volatile int mOffset = 0;
    /**
     * 标记potions的备份
     */
    private volatile int mMark = -1;
    /**
     * 当前数据占缓存的容量
     */
    private volatile int mLimit;
    /**
     * 当前缓存最大的容量
     */
    private volatile int mCapacity;

    /**
     * 临时缓存，一般用于缓存getUseBuf没有处理完
     */
    private ByteBuffer[] mTmpCacheBuf = null;
    /**
     * buf集合
     */
    private final List<ByteBuffer> mBufList;

    /**
     * 借用数量
     */
    private int mLendCount = 0;

    public MultiBuffer(byte[] data) {
        mBufList = new ArrayList<>();
        mBufSize = data.length;
        mOffset = mBufSize;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(buffer.limit());
        mBufList.add(buffer);
        mCapacity = mBufSize;
        mLimit = mBufSize;
    }

    public MultiBuffer(ByteBuffer data) {
        mBufList = new ArrayList<>();
        data.position(data.limit());
        mBufSize = data.limit();
        mOffset = mBufSize;
        mBufList.add(data);
    }

    public MultiBuffer() {
        this(true, DEFAULT_SIZE);
    }

    public MultiBuffer(boolean isDirect, int bufSize) {
        mBufList = new ArrayList<>();
        this.mBufSize = bufSize;
        mIsDirect = isDirect;
        appendBuffer();
    }


    /**
     * 扩容缓存
     */
    public void appendBuffer() {
        synchronized (mBufList) {
            if (mIsDirect) {
                mBufList.add(ByteBuffer.allocateDirect(mBufSize));
            } else {
                mBufList.add(ByteBuffer.allocate(mBufSize));
            }
            mCapacity += mBufSize;
            mLimit += mBufSize;
        }
    }


    /**
     * 获取已用的buf（含有数据的buf）
     *
     * @return
     */
    public final ByteBuffer[] getDirtyBuf() {
        return getDirtyBuf(false);
    }

    /**
     * 获取已用的buf（含有数据的buf）
     *
     * @param isFlip 为true则对每个ByteBuffer进行flip调用
     * @return
     */
    public final ByteBuffer[] getDirtyBuf(boolean isFlip) {
        synchronized (mBufList) {
            if (mLendCount > 0) {
                throw new IllegalStateException("## getUseBuf buf is use ing !!!");
            }
            int size = mBufIndex + (mOffset > 0 ? 1 : 0);
            ByteBuffer[] buffers = new ByteBuffer[size];
            for (int index = 0; index < size; index++) {
                ByteBuffer tmp = mBufList.get(index);
                if (isFlip) {
                    tmp.flip();
                }
                buffers[index] = tmp;
            }
            mLendCount = size;
            return buffers;
        }
    }


    /**
     * 获取所有的buf
     *
     * @return
     */
    public final ByteBuffer[] rentAllBuf() {
        synchronized (mBufList) {
            if (mLendCount > 0) {
                throw new IllegalStateException("## getAllBuf buf is use ing !!!");
            }
            ByteBuffer[] buffers = new ByteBuffer[mBufList.size()];
            mLendCount = buffers.length;
            return mBufList.toArray(buffers);
        }
    }


    /**
     * 租用buf（如果已租用buf则返回null）
     *
     * @return
     */
    public final ByteBuffer rentSingleBuf() {
        synchronized (mBufList) {
            if (mLendCount > 0) {
                throw new IllegalStateException("## getLendBuf buf is use ing !!!");
            }
            mLendCount = 1;
            return mBufList.get(mBufIndex);
        }
    }

    /**
     * 获取冻结的buf
     *
     * @return
     */
    public ByteBuffer[] getFreezeBuf() {
        return mTmpCacheBuf;
    }


    /**
     * 冻结buf
     *
     * @param buffer
     */
    public final void setFreezeBuf(ByteBuffer[] buffer) {
        this.mTmpCacheBuf = buffer;
    }


    /**
     * 归还buf
     *
     * @param buffer
     */
    public final void restoredBuf(ByteBuffer... buffer) {
        synchronized (mBufList) {
            if (mLendCount == 0 || buffer == null || buffer.length == 0 || mLendCount != buffer.length) {
                throw new IllegalStateException("## setBackBuf the number of returned buf is inconsistent");
            }
            for (ByteBuffer tmp : buffer) {
                if (!mBufList.contains(tmp)) {
                    // 发现不存在的buf
                    throw new IllegalStateException("## Found non-existent buf !!!");
                }
            }
            mLendCount = 0;
            for (int index = 0; index < buffer.length; index++) {
                mOffset = buffer[index].position();
                mBufIndex = index;
                if (buffer[index].hasRemaining()) {
                    // buf没有存满则认为最后的buf
                    break;
                }
            }
        }
    }

    /**
     * 把所有的buf数据装换成byte[]
     *
     * @return 把数据转换成byte数据返回
     */
    public final byte[] asByte() {
        synchronized (mBufList) {
            if (mLendCount > 0) {
                throw new IllegalStateException("currently in borrowing state,please call setBackBuf() !!!");
            }
            if (mLimit <= 0) {
                return null;
            }
            byte[] data = new byte[mLimit];
            int sumOffset = 0;

            for (int index = 0; index <= mBufIndex; index++) {
                ByteBuffer buffer = mBufList.get(index);
                int length = buffer.position();
                buffer.flip();
                buffer.get(data, sumOffset, length);
                // 恢复 limit
                buffer.limit(mBufSize);
                // 恢复 position
                buffer.position(length);
                sumOffset += length;
            }
            return data;
        }
    }

    /**
     * 当前指针位置（多个缓存buf组合）
     *
     * @return
     */
    private int position() {
        synchronized (mBufList) {
            return mBufIndex * mBufSize + mOffset;
        }
    }

    /**
     * 最大的缓存容量
     *
     * @return
     */
    public final int capacity() {
        synchronized (mBufList) {
            return mCapacity;
        }
    }

    public int size() {
        synchronized (mBufList) {
            return mBufSize;
        }
    }

    /**
     * 缓存是否还有空间可以存储数据
     *
     * @return true 还有空间
     */
    public final boolean hasRemaining() {
        synchronized (mBufList) {
            return position() < mCapacity;
        }
    }


    /**
     * 当前buf是否是没有使用
     *
     * @return
     */
    public final boolean isIdle() {
        synchronized (mBufList) {
            return mLendCount == 0;
        }
    }

    /**
     * 当前buf是否是没有数据
     *
     * @return
     */
    public final boolean isClear() {
        synchronized (mBufList) {
            return position() == 0;
        }
    }

    /**
     * 反转为读模式（数据大小position的值）
     */
    public final void flip() {
        synchronized (mBufList) {
            mLimit = position();
        }
    }

    /**
     * 判断有没有可用的容量
     *
     * @return
     */
    public final boolean isFull() {
        ByteBuffer buffer = mBufList.get(mBufIndex);
        return !buffer.hasRemaining() && mBufIndex == mBufList.size() - 1;
    }


    /**
     * 清除所有的标记
     */
    public final void clear() {
        synchronized (mBufList) {
            for (ByteBuffer buffer : mBufList) {
                buffer.clear();
            }
            mTmpCacheBuf = null;
            mLimit = mCapacity;
            mBufIndex = 0;
            mOffset = 0;
            mMark = -1;
        }
    }

    /**
     * 释放资源（由于是使用直接字节buf）
     */
    public final void release() {
        synchronized (mBufList) {
            clear();
            mBufList.clear();
        }
    }

    @Override
    public String toString() {
        return "MultilevelBuf[" +
                "bufIndex=" + mBufIndex +
                ", offset=" + mOffset +
                ", mark=" + mMark +
                ", limit=" + mLimit +
                ", capacity=" + mCapacity +
                ", bufList=" + mBufList +
                ", initSize=" + mBufSize +
                ", DEFAULT_SIZE=" + DEFAULT_SIZE +
                ", lendCount=" + mLendCount +
                ']';
    }
}
