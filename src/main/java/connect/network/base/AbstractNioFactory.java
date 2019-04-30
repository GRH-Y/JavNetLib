package connect.network.base;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNioFactory<T extends BaseNetTask> extends AbstractFactory<T> {

    protected Selector mSelector;

    /**
     * 解决线程安全问题
     */
    protected List<SelectionKey> selectionKeyList;

    abstract protected void onConnectTask(Selector selector, T task);

    abstract protected void onSelectorTask(Selector selector);

    abstract protected void onDisconnectTask(T task);

    abstract protected void onRecoveryTask(T task);


    public AbstractNioFactory() {
        mFactoryEngine = getEngine();
        if (mFactoryEngine == null) {
            setEngine(new NioFactoryEngine<T>(this));
        }
    }

    public AbstractNioFactory(FactoryEngine engine) {
        setEngine(engine);
    }

    protected FactoryEngine getEngine() {
        if (mFactoryEngine == null) {
            mFactoryEngine = new NioFactoryEngine<T>(this);
        }
        return mFactoryEngine;
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
        super.close();
        if (mSelector != null) {
            mSelector.wakeup();
        }
    }
}
