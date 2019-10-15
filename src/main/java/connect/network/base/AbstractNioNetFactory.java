package connect.network.base;


import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public abstract class AbstractNioNetFactory<T extends BaseNioNetTask> extends AbstractNetFactory<T> {

    protected Selector mSelector;

    protected abstract void onSelectionKey(SelectionKey selectionKey);

    /**
     * 准备链接
     *
     * @param task
     */
    abstract protected void onConnectTask(T task);

    /**
     * 准备断开链接回调
     *
     * @param task
     */
    abstract protected void onDisconnectTask(T task);

    /**
     * 断开链接后回调
     *
     * @param task
     */
    abstract protected void onRecoveryTask(T task);


    protected AbstractNioNetFactory() {
        setEngine(new NioEngine(this));
    }

    protected AbstractNioNetFactory(NioEngine engine) {
        setEngine(engine);
        engine.setFactory(this);
    }

    /**
     * 检查链接任务
     *
     * @param isConnectAll
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
    protected void onSelectorTask() {
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
            removerTargetTaskImp(target);
            onRecoveryTask(target);
            if (!isRemoveAll) {
                break;
            }
        }
    }

    //=====================================上面是生命周期回调方法===========================================================


    @Override
    public void addTask(T task) {
        if (task == null || mSelector == null || !mEngine.isRunning()) {
            return;
        }
        if (task.getSelectionKey() != null && !task.isTaskNeedClose()) {
            return;
        }
        if (!mConnectCache.contains(task)) {
            mConnectCache.add(task);
            mSelector.wakeup();
        }
    }

    @Override
    public void removeTask(T task) {
        if (task == null || mSelector == null || !mEngine.isRunning()) {
            return;
        }
        if (task.getSelectionKey() == null || task.isTaskNeedClose()) {
            return;
        }
        removeTaskInside(task, true);
    }

    protected void removeTaskInside(T task, boolean isNeedWakeup) {
        if (mDestroyCache.contains(task)) {
            return;
        }
        task.setTaskNeedClose(true);
        task.selectionKey.cancel();
        mDestroyCache.add(task);
        if (isNeedWakeup) {
            mSelector.wakeup();
        }
    }

    protected void destroyTaskImp() {
        //线程准备结束，释放所有链接
        for (SelectionKey selectionKey : mSelector.keys()) {
            T task = (T) selectionKey.attachment();
            onDisconnectTask(task);
            removerTargetTaskImp(task);
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

    protected void removerTargetTaskImp(T target) {
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
