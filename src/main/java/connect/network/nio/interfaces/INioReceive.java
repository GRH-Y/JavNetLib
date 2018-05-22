package connect.network.nio.interfaces;

import java.nio.channels.SocketChannel;

/**
 * 接收
 * @author yyz
 */
public interface INioReceive {
    /**
     * 读取输入流数据
     *
     * @return 成功返回读取到内容的长度, 返回-1或者0说明读取结束或者有异常
     */
    void read(SocketChannel channel);
}
