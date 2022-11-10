package com.jav.net.nio;

import com.jav.net.entity.FactoryContext;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * NioAcceptWork 只处理读写请求的事件
 *
 * @author yyz
 */
public class NioReadWriteNetWork<T extends NioClientTask, C extends SocketChannel> extends NioClientWork<T, C> {

    private List<T> mWaiteRegNetTaskList;

    protected NioReadWriteNetWork(FactoryContext context) {
        super(context);
        mWaiteRegNetTaskList = new ArrayList<>();
    }

    public void registerReadWriteEvent(T netTask) {
        synchronized (mWaiteRegNetTaskList) {
            mWaiteRegNetTaskList.add(netTask);
        }
        mSelector.wakeup();
    }

    private void regWaitNetTask() {
        synchronized (mWaiteRegNetTaskList) {
            for (T netTask : mWaiteRegNetTaskList) {
                try {
                    registerEvent(netTask, (C) netTask.getChannel());
                } catch (IOException e) {
                    callChannelError(netTask, e);
                }
            }
            mWaiteRegNetTaskList.clear();
        }
    }

    public int getPressureValue() {
        return mSelector.keys().size();
    }

    @Override
    protected void onSelectEvent() {
        int count = 0;
        try {
            count = mSelector.select();
        } catch (Exception e) {
            e.printStackTrace();
        }
        regWaitNetTask();
        if (count > 0) {
            for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                onSelectionKey(selectionKey);
            }
        }
    }

}
