package connect.network.xhttp;

import connect.network.nio.NioReceive;
import connect.network.xhttp.entity.XHttpResponse;
import util.IoEnvoy;
import util.ThreadAnnotation;

public class XHttpReceive extends NioReceive {

    public XHttpReceive(Object receive, String receiveMethod) {
        super(receive, receiveMethod);
    }

    /**
     * 读取输入流数据
     *
     * @return 如果返回false则会关闭该链接
     */
    protected void onRead() throws Exception {
        XHttpResponse response = null;
        try {
            byte[] data = IoEnvoy.tryRead(channel);
            response = XHttpResponse.parsing(data);
        } catch (Exception e) {
            response.setException(e);
            throw new Exception(e);
        } finally {
            ThreadAnnotation.disposeMessage(mReceiveMethod, mReceive, new Class[]{byte[].class, Exception.class}, new Object[]{response});
        }
    }

}
