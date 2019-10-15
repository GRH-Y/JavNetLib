package connect.network.nio;

import connect.network.base.NioEngine;

import java.nio.channels.SelectionKey;
import java.util.Iterator;

public class NioSyncClientFactory extends NioSimpleClientFactory {

    protected NioSyncClientFactory() {
        super();
    }

    protected NioSyncClientFactory(NioEngine engine) {
        super(engine);
    }


    @Override
    protected void onSelectorTask() {
        int count = 0;
        try {
            count = mSelector.select();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (count > 0) {
            synchronized (mSelector) {
                for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                    SelectionKey selectionKey = iterator.next();
                    onSelectionKey(selectionKey);
                }
            }
        }
    }

    private boolean isIteratorNext(Iterator iterator) {
        synchronized (mSelector) {
            return iterator.hasNext();
        }
    }

    private void iteratorRemove(Iterator iterator) {
        synchronized (mSelector) {
            iterator.remove();
        }
    }


    @Override
    protected void onCheckConnectTask(boolean isConnectAll) {
        while (!mConnectCache.isEmpty()) {
            NioClientTask task = null;
            if (!mConnectCache.isEmpty()) {
                task = mConnectCache.remove();
            }
            if (task == null) {
                return;
            }
            onConnectTask(task);
            if (!isConnectAll) {
                break;
            }
        }
    }

    /**
     * 检查要移除任务
     */
    @Override
    protected void onCheckRemoverTask(boolean isRemoveAll) {
        while (!mDestroyCache.isEmpty()) {
            //处理要移除的任务
            NioClientTask target = null;
            synchronized (mDestroyCache) {
                if (!mDestroyCache.isEmpty()) {
                    target = mDestroyCache.remove();
                }
            }
            if (target == null) {
                return;
            }
            onDisconnectTask(target);
            removerTargetTaskImp(target);
            onRecoveryTask(target);
            if (!isRemoveAll) {
                break;
            }
        }
    }

    @Override
    protected void destroyTaskImp() {
        synchronized (NioSyncClientFactory.class) {
            super.destroyTaskImp();
        }
    }
}
