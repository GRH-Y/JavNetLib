package com.jav.net.security.protocol;

import com.jav.common.cryption.joggle.IEncryptComponent;
import com.jav.net.security.protocol.base.ActivityCode;

import java.nio.ByteBuffer;

/**
 * 代理协议
 *
 * @author yyz
 */
public abstract class AbsProxyProtocol {


    /**
     * 发送的数据
     */
    private byte[] mSendData;


    /**
     * 操作码, 在请求状态作用于对数据的处理方式
     * 在 init 接口以下作用
     * 0x0 不加密
     * 0x1 base64
     * 0x2 aes
     * 0x3 自定义
     * 在 request 接口以下作用
     * 0 请求访问地址
     * 1 中转数据
     * <p>
     * 在响应状态作用于响应码
     * 0 正常 REP_SUCCESS_CODE
     * 1 异常 REP_EXCEPTION_CODE
     */
    private Byte mOperateCode;


    public long time() {
        return System.currentTimeMillis();
    }

    /**
     * 当前协议对应的命令类型
     *
     * @return
     * @see ActivityCode
     */
    public abstract byte activityCode();

    public Byte operateCode() {
        return mOperateCode;
    }

    public byte[] sendData() {
        return mSendData;
    }

    public void setOperateCode(byte code) {
        this.mOperateCode = code;
    }

    public void setSendData(byte[] sendData) {
        this.mSendData = sendData;
    }

    public ByteBuffer toData(IEncryptComponent encryptComponent) {
        return null;
    }


    protected ByteBuffer onEncrypt(IEncryptComponent component, ByteBuffer srcData) {
        byte[] data = srcData.array();
        byte[] encodeData = data;
        if (component != null) {
            encodeData = component.onEncrypt(data);
        }
        ByteBuffer finalData = ByteBuffer.allocate(encodeData.length + 4);
        finalData.putInt(encodeData.length);
        finalData.put(encodeData);
        return finalData;
    }
}
