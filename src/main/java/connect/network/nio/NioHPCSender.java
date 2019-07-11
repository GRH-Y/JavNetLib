package connect.network.nio;


public class NioHPCSender extends NioSender {

    @Override
    protected void onRegisterWrite() {
        NioSimpleClientFactory factory = (NioSimpleClientFactory) NioHPCClientFactory.getFactory();
        synchronized (NioHPCSender.class) {
            factory.registerWrite(this);
        }
    }

    @Override
    protected void onUnRegisterWrite() {
        NioSimpleClientFactory factory = (NioSimpleClientFactory) NioHPCClientFactory.getFactory();
        synchronized (NioHPCSender.class) {
            if (cache.isEmpty()) {
                factory.unRegisterWrite(this);
            }
        }
    }
}
