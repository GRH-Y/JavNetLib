package com.currency.net.base.joggle;

import java.nio.ByteBuffer;

public interface IAioNetReceiver {

    /**
     * 读取数据成功回调
     * @param result 状态码
     * @param byteBuffer 数据内容
     * @return 返回true则触发继续读取数据
     */
    boolean onCompleted(Integer result, ByteBuffer byteBuffer);

    /**
     * 读取失败回调
     * @param exc 异常信息
     * @param byteBuffer 数据内容
     */
    void onFailed(Throwable exc, ByteBuffer byteBuffer);
}
