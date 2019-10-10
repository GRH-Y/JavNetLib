package connect.network.base;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public abstract class AbstractNioFactory<T extends BaseNetTask> extends AbstractFactory<T> {

    protected Selector mSelector;

    protected abstract void onSelectionKey(SelectionKey selectionKey);

    abstract protected void onConnectTask(T task);

    abstract protected void onDisconnectTask(T task);

    abstract protected void onRecoveryTask(T task);


    protected AbstractNioFactory() {
        setEngine(new NioEngine(this));
    }

    protected AbstractNioFactory(NioEngine engine) {
        setEngine(engine);
        engine.setFactory(this);
    }

    /**
     * 检查链接任务
     *
     * @param isConnectAll
     */
    protected void onCheckConnectTaskImp(boolean isConnectAll) {
        while (!mConnectCache.isEmpty()) {
            T task = mConnectCache.remove();
            try {
                onConnectTask(task);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (!isConnectAll) {
                break;
            }
        }
    }

    /**
     * 获取准备好的任务
     */
    protected void onSelectorTaskImp() {
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
    protected void onCheckRemoverTaskImp(boolean isRemoveAll) {
        while (!mDestroyCache.isEmpty()) {
            //处理要移除的任务
            T target = mDestroyCache.remove();
            onDisconnectTask(target);
            removerTargetTask(target);
            onRecoveryTask(target);
            if (!isRemoveAll) {
                break;
            }
        }
    }

    //=====================================上面是生命周期回调方法===========================================================


    @Override
    public void addTask(T task) {
        if (task != null && mSelector != null && mEngine.isRunning()) {
            if (mSelector.keys().isEmpty()) {
                if (!mConnectCache.contains(task)) {
                    mConnectCache.add(task);
                    mSelector.wakeup();
                }
            } else {
                synchronized (AbstractNioFactory.class) {
                    Iterator<SelectionKey> iterator = this.mSelector.keys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = null;
                        try {
                            selectionKey = iterator.next();
                        } catch (Exception e) {
                        }
                        if (selectionKey == null) {
                            break;
                        }
                        T hasTask = (T) selectionKey.attachment();
                        if (hasTask == task) {
                            return;
                        }
                    }
                }
                if (!mConnectCache.contains(task)) {
                    mConnectCache.add(task);
                    mSelector.wakeup();
                }
            }
        }
    }

    @Override
    public void removeTask(T task) {
        if (task != null && mSelector != null && !mDestroyCache.contains(task) && mEngine.isRunning()) {
            synchronized (AbstractNioFactory.class) {
                for (SelectionKey selectionKey : mSelector.keys()) {
                    T hasTask = (T) selectionKey.attachment();
                    if (hasTask == task) {
                        selectionKey.cancel();
                        hasTask.getSocketCloseState().set(true);
                        mDestroyCache.add(task);
                        mSelector.wakeup();
                        break;
                    }
                }
            }
        }
    }

    protected void destroyTaskImp() {
        //线程准备结束，释放所有链接
        for (SelectionKey selectionKey : mSelector.keys()) {
            T task = (T) selectionKey.attachment();
            try {
                onDisconnectTask(task);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    selectionKey.cancel();
                    selectionKey.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        mConnectCache.clear();
        mDestroyCache.clear();
    }

    protected void removerTargetTask(T target) {
        synchronized (AbstractNioFactory.class) {
            for (SelectionKey selectionKey : mSelector.keys()) {
                T task = (T) selectionKey.attachment();
                if (task == target) {
                    try {
                        selectionKey.cancel();
                        selectionKey.channel().close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }


    @Override
    public void removeTask(int tag) {
        if (tag > 0 && mSelector != null && mEngine.isRunning()) {
            for (BaseNetTask task : mDestroyCache) {
                if (task.getTag() == tag) {
                    return;
                }
            }
            synchronized (AbstractNioFactory.class) {
                for (SelectionKey selectionKey : mSelector.keys()) {
                    T task = (T) selectionKey.attachment();
                    if (task.getTag() == tag) {
                        selectionKey.cancel();
                        task.getSocketCloseState().set(true);
                        mDestroyCache.add(task);
                        mSelector.wakeup();
                        break;
                    }
                }
            }
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
        super.close();
        if (mSelector != null) {
            mSelector.wakeup();
        }
    }
}
