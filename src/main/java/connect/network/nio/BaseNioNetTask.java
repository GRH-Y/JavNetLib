package connect.network.nio;

import connect.network.base.BaseNetTask;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SelectionKey;

public class BaseNioNetTask extends BaseNetTask {

    protected SelectionKey selectionKey;

    private SSLEngine sslEngine = null;

    protected void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    protected SelectionKey getSelectionKey() {
        return selectionKey;
    }

    protected void setSslEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }

    protected SSLEngine getSslEngine() {
        return sslEngine;
    }

}
