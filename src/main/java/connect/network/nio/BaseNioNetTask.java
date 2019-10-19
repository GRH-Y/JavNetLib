package connect.network.nio;

import connect.network.base.BaseNetTask;

import java.nio.channels.SelectionKey;

public class BaseNioNetTask extends BaseNetTask {

    protected SelectionKey selectionKey;

    protected void setSelectionKey(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
    }

    protected SelectionKey getSelectionKey() {
        return selectionKey;
    }

}
