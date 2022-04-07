package com.currency.net.nio;


import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.INetFactory;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.ssl.SSLFactory;

/**
 * nio客户端工厂接口创建者
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientFactory extends AbsNetFactory<NioClientTask> {

    private static final class InnerClass {
        public static final NioClientFactory sFactory = new NioClientFactory();
    }

    public NioClientFactory() {
    }

    public static INetFactory getFactory() {
        return InnerClass.sFactory;
    }

    public static void destroy() {
        InnerClass.sFactory.close();
    }


    //--------------------------- start init-----------------------------------------------
    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new NioClientWork(mFactoryIntent);
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioNetEngine(mFactoryIntent);
    }

    //--------------------------- end init-----------------------------------------------

}
