package connect.network.base;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;

public class NioEngine<T extends BaseNetTask> extends LowPcEngine {

    protected AbstractNioFactory<T> mFactory;

    protected NioEngine() {
    }

    protected NioEngine(AbstractNioFactory<T> factory) {
        this.mFactory = factory;
    }

    protected void setFactory(AbstractNioFactory<T> mFactory) {
        this.mFactory = mFactory;
    }

    @Override
    protected void onRunLoopTask() {
        //检测是否有新的任务添加
        if (!mFactory.mConnectCache.isEmpty()) {
            T task = mFactory.mConnectCache.remove();
            mFactory.onConnectTask(mFactory.mSelector, task);
        }

        int count = 0;
        try {
            count = mFactory.mSelector.select();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (count > 0) {
            mFactory.onSelectorTask(mFactory.mSelector);
        }
        //清除要结束的任务
        onRemoveNeedDestroyTask(false);
    }


    /**
     * 移除任务，nio任务没有用到mExecutorQueue集合，所以需要重写
     */
    protected void onRemoveNeedDestroyTask(boolean isRemoveAll) {
        //销毁链接
        while (!mFactory.mDestroyCache.isEmpty()) {
            T target = mFactory.mDestroyCache.remove();
            mFactory.onDisconnectTask(target);
            Iterator<SelectionKey> iterator = mFactory.mSelector.keys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                T task = (T) selectionKey.attachment();
                if (task == target) {
                    try {
                        selectionKey.channel().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        selectionKey.cancel();
                    }
                    break;
                }
            }
            mFactory.onRecoveryTask(target);
            if (!isRemoveAll) {
                break;
            }
        }
    }

    @Override
    protected void onDestroyTask() {
        //线程结束，是否要调用任务生命周期onClose方法
        for (SelectionKey selectionKey : mFactory.mSelector.keys()) {
            T task = (T) selectionKey.attachment();
            try {
                mFactory.onDisconnectTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    selectionKey.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                selectionKey.cancel();
                mFactory.onRecoveryTask(task);
            }
        }
        if (mFactory.mSelector != null) {
            try {
                mFactory.mSelector.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mFactory.mSelector.keys().clear();
            }
        }
        mFactory.mConnectCache.clear();
        mFactory.mDestroyCache.clear();
    }

    protected void removeTaskImp(int tag) {
        for (BaseNetTask task : mFactory.mDestroyCache) {
            if (task.getTag() == tag) {
                return;
            }
        }
        for (SelectionKey selectionKey : mFactory.mSelector.keys()) {
            T task = (T) selectionKey.attachment();
            if (task.getTag() == tag) {
                mFactory.mDestroyCache.add(task);
            }
        }
    }

    @Override
    protected void removeTask(int tag) {
        if (mExecutor.getLoopState()) {
            removeTaskImp(tag);
            if (mExecutor != null) {
                mExecutor.resumeTask();
            }
        }
    }

}
