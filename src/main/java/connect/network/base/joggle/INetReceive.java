package connect.network.base.joggle;

/**
 * 接收
 *
 * @author yyz
 */
public interface INetReceive<T> {

    void onReceive(T data, Exception e);
}
