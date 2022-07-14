package com.jav.net.nio;


import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.ISSLFactory;
import com.jav.net.ssl.SSLFactory;

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
