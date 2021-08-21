package connect.network.nio;

import connect.network.base.BaseTLSTask;

import java.nio.channels.SelectionKey;

public class BaseNioNetTask extends BaseTLSTask {

    protected SelectionKey mSelectionKey;

    protected void setSelectionKey(SelectionKey selectionKey) {
        this.mSelectionKey = selectionKey;
    }

    protected SelectionKey getSelectionKey() {
        return mSelectionKey;
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        mSelectionKey = null;
    }
}
