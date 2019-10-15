package connect.network.base;

import connect.network.base.joggle.INetFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 复用的工厂
 *
 * @param <T>
 */
public abstract class AbstractNetFactory<T extends BaseNetTask> implements INetFactory<T> {

    protected PcEngine mEngine;

    /**
     * 等待创建连接队列
     */
    protected Queue<T> mConnectCache;

    /**
     * 销毁任务队列
     */
    protected Queue<T> mDestroyCache;

    protected AbstractNetFactory() {
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
