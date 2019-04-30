package connect.network.base;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;

public class NioFactoryEngine<T extends BaseNetTask> extends LowPcEngine<T> {

    protected AbstractNioFactory mFactory;


    public NioFactoryEngine(AbstractNioFactory factory) {
        this.mFactory = factory;
    }

    @Override
    protected void onRunLoopTask() {
        //检测是否有新的任务添加
        if (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
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
        while (!mDestroyCache.isEmpty()) {
            T target = mDestroyCache.remove();
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
                selectionKey.cancel();
                mFactory.onRecoveryTask(task);
            }
        }
        if (mFactory.mSelector != null) {
            try {
                mFactory.mSelector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mConnectCache.clear();
        mDestroyCache.clear();
    }

    @Override
    protected void removeTask(int tag) {
        if (mExecutor.getLoopState()) {
            for (BaseNetTask task : mDestroyCache) {
                if (task.getTag() == tag) {
                    return;
                }
            }
            for (SelectionKey selectionKey : mFactory.mSelector.keys()) {
                T task = (T) selectionKey.attachment();
                if (task.getTag() == tag) {
                    mDestroyCache.add(task);
                }
            }
            if (mExecutor != null) {
                mExecutor.resumeTask();
            }
        }
    }

}
