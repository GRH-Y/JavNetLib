package connect.network.base;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NioGlobalLock {

    private Lock reentrantLock;

    private Object lock;

    private NioGlobalLock() {
        reentrantLock = new ReentrantLock();
        lock = new Object();
    }

    private static class NioGlobalLockInner {
        private static NioGlobalLock nioGlobalLock = new NioGlobalLock();
    }

    public static NioGlobalLock getInstance() {
        return NioGlobalLockInner.nioGlobalLock;
    }

//    public Object getSynchronizedLock() {
//        return lock;
//    }

    public void lock() {
        boolean ret = reentrantLock.tryLock();
        if (!ret) {
            try {
                while (!reentrantLock.tryLock()) {
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (ret) {
            reentrantLock.lock();
        }
    }

    public void unLock() {
        reentrantLock.unlock();
        synchronized (this) {
            notifyAll();
        }
    }
}
