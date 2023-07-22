package com.jav.net.security.channel.joggle;

public interface ISecurityChannelChangeListener {

    /**
     * 需要链接低负载的服务端
     *
     * @param lowLoadHost
     * @param lowLoadPort
     */
    void onRemoteLowLoadServerConnect(String lowLoadHost, int lowLoadPort);
}
