package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelEventDepot {

    private SelectionKey mNewEvent;

    private final AtomicBoolean mLockStatus;

    private final AtomicInteger mAcceptCount;

    private final AtomicInteger mConnectCount;

    private final AtomicInteger mReadCount;

//    private final AtomicInteger mWriteCount;

    private final AtomicBoolean mWriteTrigger;

    ChannelEventDepot() {
        mLockStatus = new AtomicBoolean(false);
        mAcceptCount = new AtomicInteger(0);
        mConnectCount = new AtomicInteger(0);
        mReadCount = new AtomicInteger(0);
//        mWriteCount = new AtomicInteger(0);
        mWriteTrigger = new AtomicBoolean(false);
    }

    //---------------------------------------------------------------------------

    void offerEvent(SelectionKey newEvent) {
        if (newEvent == null) {
            return;
        }
        try {
            int ops = newEvent.readyOps();
            mNewEvent = newEvent;
            if ((ops & SelectionKey.OP_ACCEPT) != 0) {
                if (mAcceptCount.get() == 0) {
                    mAcceptCount.incrementAndGet();
                }
            }
            if ((ops & SelectionKey.OP_CONNECT) != 0) {
                if (mConnectCount.get() == 0) {
                    mConnectCount.incrementAndGet();
                }
            }
            if ((ops & SelectionKey.OP_READ) != 0) {
                mReadCount.incrementAndGet();
            }
            if ((ops & SelectionKey.OP_WRITE) != 0) {
//                mWriteCount.incrementAndGet();
                mWriteTrigger.compareAndSet(false, true);
            }
        } catch (Exception e) {
        }

//        LogDog.v("#ChannelEventEntity# offerEvent newEvent ops = " + newEvent.interestOps()
//                + " readCount = " + mReadCount.get()
//                + " hasWrite = " + mWriteTrigger.get()
//                + " entity = " + this + " thread = " + Thread.currentThread().getName());
    }

    SelectionKey pollEvent() {
//        LogDog.v("#ChannelEventEntity# pollEvent newEvent ops = " + mNewEvent.interestOps() + " entity = " + this + " thread = " + Thread.currentThread().getName());
        return mNewEvent;
    }

    BaseNetTask attachment() {
        if (mNewEvent != null) {
            Object object = mNewEvent.attachment();
            if (object == null || !(object instanceof BaseNetTask<?>)) {
                LogDog.e("## ChannelEventDepot attachment netTask is null !");
                mNewEvent.cancel();
                return null;
            }
            return (BaseNetTask) object;
        }
        return null;
    }

    int readyOps() {
        int ops = 0;
        if (mNewEvent != null) {
            if (mAcceptCount.get() > 0) {
                ops += SelectionKey.OP_ACCEPT;
            }
            if (mConnectCount.get() > 0) {
                ops += SelectionKey.OP_CONNECT;
            }
            if (mReadCount.get() > 0) {
                ops += SelectionKey.OP_READ;
            }
            if (mWriteTrigger.get()) {
                ops += SelectionKey.OP_WRITE;
            }
        }
        return ops;
    }

    SelectableChannel channel() {
        if (mNewEvent != null) {
            return mNewEvent.channel();
        }
        return null;
    }

    void depleteEvent(int ops) {
        if ((ops & SelectionKey.OP_ACCEPT) != 0) {
            mAcceptCount.set(-1);
        }
        if ((ops & SelectionKey.OP_CONNECT) != 0) {
            mConnectCount.set(-1);
        }
        if ((ops & SelectionKey.OP_READ) != 0) {
            mReadCount.decrementAndGet();
        }
        if ((ops & SelectionKey.OP_WRITE) != 0) {
//                mWriteCount.decrementAndGet();
            mWriteTrigger.set(false);
        }
//        LogDog.v("#ChannelEventEntity# doneEvent "
//                + " readCount = " + mReadCount.get()
//                + " hasWrite = " + mWriteTrigger.get());
    }

    boolean hasEvent() {
        if (mNewEvent == null || !mNewEvent.isValid()) {
            return false;
        }
        BaseNetTask netTask = attachment();
        if (netTask != null) {
            IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
            if (stateMachine.isAttachState(NetTaskStatus.FINISHING) || stateMachine.getState() == NetTaskStatus.INVALID) {
                mNewEvent.cancel();
                return false;
            }
        }
        return mAcceptCount.get() > 0 || mConnectCount.get() > 0 || mReadCount.get() > 0 || mWriteTrigger.get();
    }


    //---------------------------------------------------------------------------

    boolean lockDepot() {
        return mLockStatus.compareAndSet(false, true);
    }

    void unLockDepot() {
        mLockStatus.compareAndSet(true, false);
    }

    boolean isLockStatus() {
        return mLockStatus.get();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ChannelEventEntity{\n");
        sb.append("mNewEvent=");
        sb.append(mNewEvent);
        sb.append(",\n mLockStatus=");
        sb.append(mLockStatus);
        sb.append(",\n mAcceptCount=");
        sb.append(mAcceptCount);
        sb.append(",\n mConnectCount=");
        sb.append(mConnectCount);
        sb.append(",\n mReadCount=");
        sb.append(mReadCount);
        sb.append(",\n mWriteTrigger=");
        sb.append(mWriteTrigger);
        sb.append("\n}");
        return sb.toString();

    }
}