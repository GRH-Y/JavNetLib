package connect.network.base;

import connect.network.base.joggle.IFactory;
import connect.network.base.joggle.ISSLFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 复用的工厂
 *
 * @param <T>
 */
public abstract class AbstractFactory<T extends BaseNetTask> implements IFactory<T> {

    protected PcEngine mEngine;
    protected ISSLFactory mSslFactory = null;

    /**
     * 等待创建连接队列
     */
    protected Queue<T> mConnectCache;

    /**
     * 销毁任务队列
     */
    protected Queue<T> mDestroyCache;

    protected AbstractFactory() {
        mConnectCache = new ConcurrentLinkedQueue<>();
        mDestroyCache = new ConcurrentLinkedQueue<>();
    }

    protected void setEngine(PcEngine engine) {
        this.mEngine = engine;
    }

    //-----------------------------------------------------------------------------------------

    @Override
    public void addTask(T task) {
        if (task != null && !mConnectCache.contains(task) && mEngine.isRunning()) {
            mConnectCache.add(task);
            mEngine.resumeTask();
        }
    }

    @Override
    public void removeTask(T task) {
        if (task != null && !mDestroyCache.contains(task) && mEngine.isRunning()) {
            //该任务在此线程才能添加
            mDestroyCache.add(task);
            mEngine.resumeTask();
        }
    }

    @Override
    public void removeTask(int tag) {
    }

    @Override
    public void setSSlFactory(ISSLFactory sslFactory) {
        this.mSslFactory = sslFactory;
    }

    //-----------------------------------------------------------------------------------------

    @Override
    public void open() {
        mEngine.startEngine();
    }

    @Override
    public void close() {
        mEngine.stopEngine();
    }

}
