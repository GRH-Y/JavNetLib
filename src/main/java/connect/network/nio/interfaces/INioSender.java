package connect.network.nio.interfaces;


import java.nio.channels.SocketChannel;

/**
 * 发送者接口
 *
 * @author yyz
 */
public interface INioSender {
    /**
     * 向输出流写数据
     *
     * @return 成功发送返回true
     */
    void write(SocketChannel channel);
}
