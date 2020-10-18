package connect.network.nio;

import connect.network.base.BaseTLSTask;

import java.nio.channels.SelectionKey;

public class BaseNioNetTask extends BaseTLSTask {

    protected SelectionKey selectionKey;

    protected void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    protected SelectionKey getSelectionKey() {
        return selectionKey;
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        selectionKey = null;
    }
}
