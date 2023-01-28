package com.jav.net.security.channel.base;

import com.jav.net.security.channel.joggle.IClientEventCallBack;
import com.jav.net.security.channel.joggle.IServerEventCallBack;
import com.jav.net.security.channel.joggle.ISyncServerEventCallBack;

/**
 * 解析器回调注册器
 *
 * @author yyz
 */
public class ParserCallBackRegistrar {

    private IClientEventCallBack mClientCallBack;
    private IServerEventCallBack mServerCallBack;
    private ISyncServerEventCallBack mSyncCallBack;

    public ParserCallBackRegistrar(IClientEventCallBack callBack) {
        mClientCallBack = callBack;
    }

    public ParserCallBackRegistrar(IServerEventCallBack callBack) {
        mServerCallBack = callBack;
    }

    public ParserCallBackRegistrar(ISyncServerEventCallBack callBack) {
        mSyncCallBack = callBack;
    }


    public IClientEventCallBack getClientCallBack() {
        return mClientCallBack;
    }

    public IServerEventCallBack getServerCallBack() {
        return mServerCallBack;
    }

    public ISyncServerEventCallBack getSyncCallBack() {
        return mSyncCallBack;
    }
}
