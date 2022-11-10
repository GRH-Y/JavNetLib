package com.jav.net.nio;


import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.ssl.SSLComponent;

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

    public static INetFactory<NioClientTask> getFactory() {
        return InnerClass.sFactory;
    }

    public static void destroy() {
        InnerClass.sFactory.close();
    }

    //--------------------------- start init-----------------------------------------------

    @Override
    protected ISSLComponent initSSLComponent() {
        return new SSLComponent();
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new NioClientWork(mFactoryContext);
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioNetEngine(mFactoryContext);
    }

    //--------------------------- end init-----------------------------------------------

}
