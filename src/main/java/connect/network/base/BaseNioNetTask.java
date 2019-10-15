package connect.network.base;

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
