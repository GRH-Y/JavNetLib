package connect.network.nio;

import connect.network.base.BaseNetWork;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public abstract class NioNetWork<T extends BaseNioNetTask> extends BaseNetWork<T> {

    protected Selector mSelector;

    protected void init() {
        if (mSelector == null) {
            try {
                //use time 310ms
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
        super.onCheckConnectTask();
    }

    /**
     * 获取准备好的任务
     */
    @Override
    protected void onExecuteTask() {
        int count = 0;
        if (mConnectCache.isEmpty() && mDestroyCache.isEmpty()) {
            try {
                count = mSelector.select();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                count = mSelector.selectNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (count > 0) {
            for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                onSelectionKey(selectionKey);
            }
        }
    }

    @Override
    protected void onCheckRemoverTask() {
        super.onCheckRemoverTask();
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
    protected void onRecoveryTaskAll() {
        if (mSelector != null) {
            //线程准备结束，释放所有链接
            for (SelectionKey selectionKey : mSelector.keys()) {
                if (selectionKey.isValid()) {
                    T task = (T) selectionKey.attachment();
                    removerTaskImp(task);
                }
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
}
