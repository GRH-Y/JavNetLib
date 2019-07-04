package connect.network.base;


import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public abstract class AbstractNioFactory<T extends BaseNetTask> extends AbstractFactory<T> {

    protected Selector mSelector;

    abstract protected void onConnectTask(Selector selector, T task);

    abstract protected void onSelectorTask(Selector selector);

    abstract protected void onDisconnectTask(T task);

    abstract protected void onRecoveryTask(T task);


    protected AbstractNioFactory() {
        setEngine(new NioEngine(this));
    }

    protected AbstractNioFactory(NioEngine engine) {
        setEngine(engine);
        engine.setFactory(this);
    }


    @Override
    public void addTask(T task) {
        if (task != null && mSelector != null) {
            Iterator<SelectionKey> iterator = mSelector.keys().iterator();
            while (iterator.hasNext()) {
                try {
                    SelectionKey selectionKey = iterator.next();
                    T hasTask = (T) selectionKey.attachment();
                    if (hasTask == task) {
                        return;
                    }
                } catch (Exception e) {
                    break;
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
