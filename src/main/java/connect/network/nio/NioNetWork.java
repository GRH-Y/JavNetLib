package connect.network.nio;

import connect.network.base.BaseNetWork;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public abstract class NioNetWork<T extends BaseNioNetTask> extends BaseNetWork<T> {

    protected Selector mSelector;

    protected NioNetWork() {
        try {
            mSelector = Selector.open();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    protected Selector getSelector() {
        return mSelector;
    }


    /**
     * 检查要链接任务
     */
    protected void onCheckConnectTask(boolean isConnectAll) {
        while (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            onConnectTask(task);
            if (!isConnectAll) {
                break;
            }
        }
    }

    /**
     * 获取准备好的任务
     */
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
    protected void onCheckRemoverTask(boolean isRemoveAll) {
        while (!mDestroyCache.isEmpty()) {
            //处理要移除的任务
            T target = mDestroyCache.remove();
            onDisconnectTask(target);
            closeConnect(target);
            if (!isRemoveAll) {
                break;
            }
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
        super.onRecoveryTask(task);
        task.setSelectionKey(null);
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
        }
        mConnectCache.clear();
        mDestroyCache.clear();
    }

    //------------------------------------------------------------------------------------------------------------------

    protected void closeConnect(T target) {
        try {
            if (target.selectionKey != null) {
                target.selectionKey.cancel();
                SocketChannel channel = (SocketChannel) target.selectionKey.channel();
                Socket socket = channel.socket();
                socket.shutdownInput();
                socket.shutdownOutput();
                channel.close();
                socket.close();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            onRecoveryTask(target);
        }
    }

}
