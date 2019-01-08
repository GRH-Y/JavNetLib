package connect.network.nio;


import connect.network.base.AbstractFactory;
import connect.network.base.FactoryCoreTask;
import connect.network.tcp.AbstractTcpFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public abstract class AbstractNioFactory<T> extends AbstractTcpFactory<T> {

    protected Selector mSelector;

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
            Iterator<SelectionKey> iterator = mSelector.keys().iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                T hasTask = (T) selectionKey.attachment();
                if (hasTask == task) {
                    return;
                }
            }
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
    public void open() {
        if (mSelector == null) {
            try {
                mSelector = Selector.open();
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


    private class NioCoreTask<K> extends FactoryCoreTask<K> {

        private NioCoreTask(AbstractFactory factory) {
            super(factory);
        }

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
                        }
                        selectionKey.cancel();
                        break;
                    }
                }
                onRecoveryTask((T) target);
            }
        }

        @Override
        protected void onCreateData() {
        }

        @Override
        protected void onProcess() {
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
