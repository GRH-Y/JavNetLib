package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.net.base.joggle.IRegisterSelectorEvent;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SelectorEventHubs implements IRegisterSelectorEvent {

    private final Map<SelectableChannel, ChannelEventDepot> mChannelEventCache;

    private final ReentrantLock mSelectLock;

    private Selector mSelector;

    private final AtomicInteger mHasEventCount;

    private int mIndex = 0;


    public SelectorEventHubs() {
        mHasEventCount = new AtomicInteger(0);
        mChannelEventCache = new ConcurrentHashMap<>();
        mSelectLock = new ReentrantLock(false);
        try {
            mSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void pushClassificationEventQueue(SelectionKey newEvent) {
        SelectableChannel channel = newEvent.channel();
        synchronized (mHasEventCount) {
            ChannelEventDepot eventEntity = mChannelEventCache.get(channel);
            if (eventEntity == null) {
                eventEntity = new ChannelEventDepot();
                eventEntity.offerEvent(newEvent);
                mChannelEventCache.put(channel, eventEntity);
            } else {
                eventEntity.offerEvent(newEvent);
            }
        }
//        String[] className = toString().split("\\.");
//        LogDog.w("#pushClassificationEventQueue# newEvent = " + newEvent.readyOps() + " this = " + className[className.length - 1]);
    }

    /**
     * 检测队列中所有depot是否存在未锁定状态的
     *
     * @return true 表示存在
     */
    private boolean checkIdleDepot() {
        Collection<ChannelEventDepot> collection = mChannelEventCache.values();
        Iterator<ChannelEventDepot> iterator = collection.iterator();
        while (iterator.hasNext()) {
            ChannelEventDepot depot = iterator.next();
            if (!depot.isLockStatus()) {
                return true;
            }
        }
        return false;
    }


    private ChannelEventDepot getAvailableEvent() {
        if (!mSelector.isOpen() || mChannelEventCache.isEmpty()) {
            return null;
        }
        Collection<ChannelEventDepot> collection = mChannelEventCache.values();
        synchronized (this) {
            ChannelEventDepot[] values = new ChannelEventDepot[collection.size()];
            values = collection.toArray(values);
            if (values.length == 0) {
                return null;
            }
            if (mIndex >= values.length) {
                mIndex = 0;
            }
//            LogDog.i("==> getAvailableEvent start ... , work = " + Thread.currentThread().getName());
            ChannelEventDepot eventDepot = values[mIndex];
            if (eventDepot == null) {
                return null;
            }
            if (!eventDepot.hasEvent() && !eventDepot.isLockStatus()) {
                //该通道事件处理完毕,清除出队列
                collection.remove(eventDepot);
                mHasEventCount.decrementAndGet();
                LogDog.e("## getAvailableEvent has null event Depot !!!");
                return null;
            }
            if (eventDepot.isLockStatus() || !eventDepot.lockDepot()) {
//                LogDog.i("==> getAvailableEvent not event end ... , work = " + Thread.currentThread().getName());
                mIndex++;
                return null;
            }
//            LogDog.i("==> choose channel = " + eventDepot);
//            LogDog.i("==> getAvailableEvent has event end ... , work = " + java.lang.Thread.currentThread().getName() + " mIndex = " + mIndex);
            mIndex++;
            return eventDepot;
        }
    }

    private void selectEvent() {
        if (mHasEventCount.get() > 0 && checkIdleDepot() && !mSelectLock.tryLock()) {
            //当前状态是事件队列非空，有空闲的depot 并且已经有线程正在处理selector，则跳出处理事件队列事件
//            LogDog.i("==> selectEvent has eventCount = " + mHasEventCount.get());
            return;
        } else {
//            LogDog.i("==>  lock selectEvent , thread = " + Thread.currentThread().getName());
            mSelectLock.lock();
        }
        try {
            if (mSelector.isOpen()) {
//                LogDog.i("==> start selectEvent ...");
                int eventCount;
                if (checkIdleDepot()) {
                    //有空闲的depot不能阻塞
                    eventCount = mSelector.selectNow();
                } else {
                    eventCount = mSelector.select();
                }
                if (eventCount > 0) {
                    for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                         iterator.hasNext() && mSelector.isOpen(); iterator.remove()) {
                        SelectionKey selectionKey = iterator.next();
                        pushClassificationEventQueue(selectionKey);
                    }
                    mHasEventCount.set(mChannelEventCache.size());
                }
//                LogDog.i("==> selectEvent eventCount = " + eventCount);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
//            LogDog.i("==>  unlock selectEvent , thread = " + Thread.currentThread().getName());
            mSelectLock.unlock();
        }
    }

    public void doneChannelEvent(ChannelEventDepot eventDepot) {
//        eventDepot.depleteEvent();
        synchronized (mHasEventCount) {
            if (!eventDepot.hasEvent()) {
                //该通道事件处理完毕,清除出队列
                SelectionKey selectionKey = eventDepot.pollEvent();
                if (selectionKey != null) {
                    SelectableChannel key = selectionKey.channel();
                    mChannelEventCache.remove(key);
                    mHasEventCount.decrementAndGet();
////                    LogDog.i("==> doneChannelEvent has eventCount = " + mHasEventCount.get());
                }
            }
        }
        eventDepot.unLockDepot();
    }


    public ChannelEventDepot getReadyChannelEvent(boolean isBlock) {
        ChannelEventDepot newEvent;
        do {
            selectEvent();
            newEvent = getAvailableEvent();
        } while (newEvent == null && isBlock && mSelector.isOpen());
        return newEvent;
    }


    public List<SelectionKey> getRegisterKey() {
        if (!mSelector.isOpen()) {
            return null;
        }
        Set<SelectionKey> keys = mSelector.keys();
        return new ArrayList<>(keys);
    }

    @Override
    public SelectionKey registerAcceptEvent(SelectableChannel channel, Object att) throws IOException {
        SelectionKey selectionKey = null;
        if (channel != null) {
            selectionKey = channel.register(mSelector, SelectionKey.OP_ACCEPT, att);
            mSelector.wakeup();
        }
        return selectionKey;
    }

    @Override
    public SelectionKey registerConnectEvent(SelectableChannel channel, Object att) throws IOException {
        SelectionKey selectionKey = null;
        if (channel != null) {
            selectionKey = channel.register(mSelector, SelectionKey.OP_CONNECT, att);
            mSelector.wakeup();
        }
        return selectionKey;
    }

    @Override
    public SelectionKey registerReadEvent(SelectableChannel channel, Object att) throws IOException {
        SelectionKey selectionKey = null;
        if (channel != null) {
            selectionKey = channel.register(mSelector, SelectionKey.OP_READ, att);
            mSelector.wakeup();
        }
        return selectionKey;
    }

    @Override
    public SelectionKey registerWriteEvent(SelectableChannel channel, Object att) throws IOException {
        SelectionKey selectionKey = null;
        if (channel != null) {
            selectionKey = channel.register(mSelector, SelectionKey.OP_WRITE, att);
            mSelector.wakeup();
        }
        return selectionKey;
    }

    public void destroy() {
        if (mSelector != null) {
            mSelector.wakeup();
            mSelectLock.lock();
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mSelectLock.unlock();
            }
        }
        mChannelEventCache.clear();
    }
}
