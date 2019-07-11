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

    private SelectionKey getSelectionKey() {
        SelectionKey selectionKey = null;
        synchronized (mSelector) {
            Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator();
            if (iterator.hasNext()) {
                selectionKey = iterator.next();
                iterator.remove();
            }
        }
        return selectionKey;
    }

    @Override
    protected void onSelectorTaskImp() {
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


    @Override
    protected void onCheckConnectTaskImp(boolean isConnectAll) {
        while (!mConnectCache.isEmpty()) {
            NioClientTask task = null;
            synchronized (mConnectCache) {
                if (!mConnectCache.isEmpty()) {
                    task = mConnectCache.remove();
                }
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
    protected void onCheckRemoverTaskImp(boolean isRemoveAll) {
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
            removerTargetTask(target);
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
