package connect.network.nio;

import connect.network.base.BaseNetWork;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public abstract class NioNetWork<T extends BaseNioNetTask> extends BaseNetWork<T> {

    protected Selector mSelector;

    protected void init() {
        if (mSelector == null) {
            try {
                mSelector = Selector.open();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    protected Selector getSelector() {
        return mSelector;
    }

    /**
     * 检查要链接任务
     */
    @Override
    protected void onCheckConnectTask() {
        if (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            onConnectTask(task);
        }
    }

    /**
     * 获取准备好的任务
     */
    @Override
    protected void onExecuteTask() {
        int count = 0;
        try {
            count = mSelector.select();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (count > 0) {
            for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                onSelectionKey(selectionKey);
            }
        }
    }


    /**
     * 检查要移除任务
     */
    @Override
    protected void onCheckRemoverTask() {
        if (!mDestroyCache.isEmpty()) {
            //处理要移除的任务
            T target = mDestroyCache.remove();
            onDisconnectTask(target);
            closeConnect(target);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 处理通道事件
     *
     * @param selectionKey
     */
    protected void onSelectionKey(SelectionKey selectionKey) {
    }

    @Override
    public void onRecoveryTask(T task) {
        task.setSelectionKey(null);
        super.onRecoveryTask(task);
    }

    @Override
    protected void onRecoveryTaskAll() {
        if (mSelector != null) {
            //线程准备结束，释放所有链接
            for (SelectionKey selectionKey : mSelector.keys()) {
                T task = (T) selectionKey.attachment();
                onDisconnectTask(task);
                closeConnect(task);
            }
            try {
                mSelector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSelector = null;
        }
        mConnectCache.clear();
        mDestroyCache.clear();
    }

    //------------------------------------------------------------------------------------------------------------------

    protected void closeConnect(T target) {
        if (target.selectionKey != null) {
            try {
                SelectableChannel channel = target.selectionKey.channel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                target.selectionKey.cancel();
            }
        }
        onRecoveryTask(target);
    }
}
