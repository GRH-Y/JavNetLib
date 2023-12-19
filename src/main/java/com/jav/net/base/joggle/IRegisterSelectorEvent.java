package com.jav.net.base.joggle;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public interface IRegisterSelectorEvent {

    /**
     * 注册Accept 事件
     *
     * @param channel
     * @param att
     * @return
     * @throws ClosedChannelException
     */
    SelectionKey registerAcceptEvent(SelectableChannel channel, Object att) throws IOException;

    /**
     * 注册 Connect 事件
     *
     * @param channel
     * @param att
     * @return
     * @throws ClosedChannelException
     */
    SelectionKey registerConnectEvent(SelectableChannel channel, Object att) throws IOException;

    /**
     * 注册 Read 事件
     *
     * @param channel
     * @param att
     * @return
     * @throws ClosedChannelException
     */
    SelectionKey registerReadEvent(SelectableChannel channel, Object att) throws IOException;

    /**
     * 注册 Write 事件
     *
     * @param channel
     * @param att
     * @return
     * @throws ClosedChannelException
     */
    SelectionKey registerWriteEvent(SelectableChannel channel, Object att) throws IOException;


}
