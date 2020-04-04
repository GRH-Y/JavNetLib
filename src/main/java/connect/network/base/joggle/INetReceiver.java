package connect.network.base.joggle;

/**
 * 接收
 *
 * @author yyz
 */
public interface INetReceiver<T> {

    void onReceive(T data, Exception e);
}
