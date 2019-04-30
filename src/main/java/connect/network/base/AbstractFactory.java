package connect.network.base;

import connect.network.base.joggle.IFactory;
import connect.network.base.joggle.ISSLFactory;

/**
 * 复用的工厂
 *
 * @param <T>
 */
public abstract class AbstractFactory<T extends BaseNetTask> implements IFactory<T> {

    protected FactoryEngine mFactoryEngine;
    protected ISSLFactory mSslFactory = null;

    protected void setEngine(FactoryEngine engine) {
        this.mFactoryEngine = engine;
    }

    protected abstract FactoryEngine getEngine();

    @Override
    public void addTask(T task) {
        mFactoryEngine.addTask(task);
    }

    @Override
    public void removeTask(T task) {
        mFactoryEngine.removeTask(task);
    }

    @Override
    public void removeTask(int tag) {
        mFactoryEngine.removeTask(tag);
    }

    @Override
    public void setSSlFactory(ISSLFactory sslFactory) {
        this.mSslFactory = sslFactory;
    }

    @Override
    public void open() {
        mFactoryEngine.startEngine();
    }

    @Override
    public void openHighPer() {
        mFactoryEngine.openHighPer();
    }

    @Override
    public void close() {
        mFactoryEngine.stopEngine();
    }

}
