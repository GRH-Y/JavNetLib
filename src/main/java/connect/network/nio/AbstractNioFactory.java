package connect.network.nio;


import connect.network.base.AbstractFactory;
import connect.network.base.BaseNetTask;
import connect.network.base.FactoryCoreTask;
import connect.network.tcp.AbstractTcpFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractNioFactory<T extends BaseNetTask> extends AbstractTcpFactory<T> {

    protected Selector mSelector;

    /**
     * 解决线程安全问题
     */
    protected List<SelectionKey> selectionKeyList;

    abstract protected void onConnectTask(Selector selector, T task);

    abstract protected void onSelectorTask(Selector selector);

    abstract protected void onDisconnectTask(T task);

    abstract protected void onRecoveryTask(T task);

    @Override
    protected boolean onConnectTask(T task) {
        return false;
    }

    @Override
    protected void onExecRead(T task) {
    }

    @Override
    protected void onExecWrite(T task) {
    }

    public AbstractNioFactory() {
        setFactoryCoreTask(new NioCoreTask(this));
    }

    @Override
    public void addTask(T task) {
        if (task != null && mSelector != null) {
            for (SelectionKey selectionKey : selectionKeyList) {
                T hasTask = (T) selectionKey.attachment();
                if (hasTask == task) {
                    return;
                }
            }
//            mSelector.keys().forEach(selectionKey -> {
//                T hasTask = (T) selectionKey.attachment();
//                if (hasTask == task) {
//                    return;
//                }
//            });
//            Iterator<SelectionKey> iterator = mSelector.keys().iterator();
//            while (iterator.hasNext()) {
//                SelectionKey selectionKey = iterator.next();
//                T hasTask = (T) selectionKey.attachment();
//                if (hasTask == task) {
//                    return;
//                }
//            }
            super.addTask(task);
            mSelector.wakeup();
        }
    }

    @Override
    public void removeTask(T task) {
        if (task != null && mSelector != null) {
            super.removeTask(task);
            mSelector.wakeup();
        }
    }

    @Override
    public void removeTask(int tag) {
        if (tag > 0 && mSelector != null) {
            super.removeTask(tag);
            mSelector.wakeup();
        }
    }


    @Override
    public void open() {
        if (mSelector == null) {
            try {
                mSelector = Selector.open();
                selectionKeyList = new ArrayList<>(mSelector.keys());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.open();
    }

    @Override
    public void close() {
        if (coreTask != null) {
            coreTask.getExecutor().stopTask();
        }
        if (mSelector != null) {
            mSelector.wakeup();
        }
    }


    private class NioCoreTask<K extends BaseNetTask> extends FactoryCoreTask<K> {

        private NioCoreTask(AbstractFactory factory) {
            super(factory);
        }

        /**
         * 根据任务tag移除任务，nio任务没有用到mExecutorQueue集合，所以需要重写
         * @param tag
         */
        @Override
        public void removeNeedDestroyTask(int tag) {
            for (BaseNetTask task : mDestroyCache) {
                if (task.getTag() == tag) {
                    return;
                }
            }
            Iterator<SelectionKey> iterator = mSelector.keys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                BaseNetTask task = (K) selectionKey.attachment();
                if (task.getTag() == tag) {
                    mDestroyCache.add((K) task);
                    break;
                }
            }
        }

        /**
         * 移除任务，nio任务没有用到mExecutorQueue集合，所以需要重写
         */
        @Override
        protected void onRemoveNeedDestroyTask() {
            //销毁链接
            while (!mDestroyCache.isEmpty()) {
                K target = mDestroyCache.remove();
                onDisconnectTask((T) target);
                Iterator<SelectionKey> iterator = mSelector.keys().iterator();
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
                onRecoveryTask((T) target);
            }
        }

        @Override
        protected void onCreateData() {
            //一定要重写改方法，nio任务不需要用到 mExecutorQueue 集合
        }

        @Override
        protected void onProcess() {
            //一定要重写改方法，nio任务不需要用到 mExecutorQueue 集合
        }

        @Override
        protected void onRunLoopTask() {
            //检测是否有新的任务添加
            while (!mConnectCache.isEmpty()) {
                K task = mConnectCache.remove();
                onConnectTask(mSelector, (T) task);
            }

            int count = 0;
            try {
                count = mSelector.select();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (count > 0) {
                onSelectorTask(mSelector);
            }
            //清除要结束的任务
            onRemoveNeedDestroyTask();
        }

        @Override
        protected void onDestroyTask() {
            //线程结束，是否要调用任务生命周期onClose方法
            for (SelectionKey selectionKey : mSelector.keys()) {
                T task = (T) selectionKey.attachment();
                try {
                    onDisconnectTask(task);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    selectionKey.cancel();
                    onRecoveryTask(task);
                }
            }
            if (mSelector != null) {
                try {
                    mSelector.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
