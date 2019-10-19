package connect.network.udp;

import connect.network.base.AbsNetEngine;
import connect.network.base.AbsNetFactory;
import connect.network.base.BaseNetWork;
import connect.network.tcp.BioEngine;

public class UdpFactory extends AbsNetFactory<UdpTask> {

    private static UdpFactory mFactory;

    private UdpFactory() {
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BioEngine(this, getNetWork());
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new UdpWork(this);
    }

    public synchronized static UdpFactory getFactory() {
        if (mFactory == null) {
            synchronized (UdpFactory.class) {
                if (mFactory == null) {
                    mFactory = new UdpFactory();
                }
            }
        }
        return mFactory;
    }

    public static void destroy() {
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }
}
