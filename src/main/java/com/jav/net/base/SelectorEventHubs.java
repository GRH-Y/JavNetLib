package com.jav.net.base;

import com.jav.net.base.joggle.IRegisterSelectorEvent;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class SelectorEventHubs implements IRegisterSelectorEvent {

    private final Map<String, ChannelEventDepot> mChannelEventMap;

    private final LinkedList<ChannelEventDepot> mEventDepotList;

    private final ReentrantLock mSelectLock;

    private Selector mSelector;


    public SelectorEventHubs() {
        mEventDepotList = new LinkedList<>();
        mChannelEventMap = new HashMap<>();
        mSelectLock = new ReentrantLock(true);
        try {
            mSelector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getChannelUniqueId(SelectionKey newEvent) {
        SelectableChannel selectableChannel = newEvent.channel();
        NetworkChannel networkChannel = (NetworkChannel) selectableChannel;
        String key = null;
        try {
            SocketAddress localAddress = networkChannel.getLocalAddress();
            if (localAddress != null) {
                key = localAddress.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (selectableChannel instanceof SocketChannel) {
            SocketChannel socketChannel = (SocketChannel) selectableChannel;
            try {
                SocketAddress socketAddress = socketChannel.getRemoteAddress();
                if (socketAddress != null) {
                    key += socketAddress.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return key;
    }


    private ChannelEventDepot pushEventQueue(SelectionKey newEvent) {
        String key = getChannelUniqueId(newEvent);
        if (key == null) {
            newEvent.cancel();
            return null;
        }
        ChannelEventDepot eventDepot;
        synchronized (mChannelEventMap) {
            eventDepot = mChannelEventMap.get(key);
            if (eventDepot == null) {
                eventDepot = new ChannelEventDepot();
                mChannelEventMap.put(key, eventDepot);
            }
            eventDepot.offerEvent(newEvent);
            synchronized (mEventDepotList) {
                mEventDepotList.offerFirst(eventDepot);
            }
//            LogDog.i("==> pushEventQueue newEvent = " + newEvent + " eventDepot = " + eventDepot + " key = " + key
//                    + " thread = " + Thread.currentThread().getName());
        }
        return eventDepot;
    }

    /**
     * 检测队列中所有depot是否存在未锁定状态的
     *
     * @return true 表示存在
     */
    private ChannelEventDepot getIdleDepot() {
        synchronized (mEventDepotList) {
            for (int index = 0; index < mEventDepotList.size(); index++) {
                ChannelEventDepot eventDepot = mEventDepotList.get(index);
                if (eventDepot == null) {
                    continue;
                }
                if (!eventDepot.hasEvent() && !eventDepot.isLockStatus()) {
//                LogDog.w("## getAvailableEvent has null event Depot ! work = " + Thread.currentThread().getName());
                    continue;
                }
                if (eventDepot.lockDepot()) {
                    return eventDepot;
                }
            }
        }
        return null;
    }


    private ChannelEventDepot selectEvent() {
        ChannelEventDepot selectEventDepot = getIdleDepot();
        if (selectEventDepot != null) {
//            LogDog.i("==> has idle Depot, selectEventDepot = " + selectEventDepot + " thread = " + Thread.currentThread().getName());
            return selectEventDepot;
        }
        mSelectLock.lock();
//        LogDog.i("==========================> lock selectEvent , thread = " + Thread.currentThread().getName());
        try {
            if (mSelector.isOpen()) {
                selectEventDepot = getIdleDepot();
                if (selectEventDepot != null) {
//                    LogDog.i("==> enter lock has idle Depot, selectEventDepot " + selectEventDepot
//                            + " thread = " + Thread.currentThread().getName());
                    return selectEventDepot;
                }
                int eventCount = mSelector.select();
                if (eventCount <= 0) {
                    return null;
                }
                for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
                     iterator.hasNext() && mSelector.isOpen(); iterator.remove()) {
                    SelectionKey selectionKey = iterator.next();
                    ChannelEventDepot eventDepot = pushEventQueue(selectionKey);
                    if (selectEventDepot == null && eventDepot.lockDepot()) {
                        selectEventDepot = eventDepot;
                    }
                }
//                LogDog.i("==> selectEvent over eventCount = " + eventCount + " selectEventDepot = " + selectEventDepot
//                        + " ChannelEventMap size = " + mChannelEventMap.size() + " thread = " + Thread.currentThread().getName());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
//            LogDog.i("=================> unlock selectEvent , thread = " + Thread.currentThread().getName());
            mSelectLock.unlock();
        }
        return selectEventDepot;
    }

    public void doneChannelEvent(ChannelEventDepot eventDepot) {
        synchronized (mChannelEventMap) {
            if (!eventDepot.hasEvent()) {
                //该通道事件处理完毕,清除出队列
                Collection<ChannelEventDepot> collection = mChannelEventMap.values();
                collection.remove(eventDepot);
                synchronized (mEventDepotList) {
                    mEventDepotList.remove(eventDepot);
                }
//                LogDog.i("clear eventDepot :" + eventDepot + " mChannelEventMap size = " + mChannelEventMap.size()
//                        + " thread :" + Thread.currentThread().getName());
            }
        }
        eventDepot.unLockDepot();
//        LogDog.i("doneChannelEvent " + eventDepot);
    }


    public ChannelEventDepot getReadyChannelEvent(boolean isBlock) {
        ChannelEventDepot newEvent;
        do {
            newEvent = selectEvent();
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
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        synchronized (mChannelEventMap) {
            mChannelEventMap.clear();
        }
    }
}
