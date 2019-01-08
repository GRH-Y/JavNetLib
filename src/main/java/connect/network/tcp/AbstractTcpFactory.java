package connect.network.tcp;

import connect.network.base.AbstractFactory;
import connect.network.base.joggle.ISSLFactory;

public abstract class AbstractTcpFactory<T> extends AbstractFactory<T> {


    protected ISSLFactory mSslFactory = null;

    @Override
    public void setSSlFactory(ISSLFactory sslFactory) {
        this.mSslFactory = sslFactory;
    }

    @Override
    protected void onExecWrite(T task) {
    }

}
