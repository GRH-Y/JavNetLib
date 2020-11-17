package connect.network.base.joggle;

/**
 * 接收
 *
 * @author yyz
 */
public interface INetReceiver<T> {

    void onReceiveFullData(T buf, Throwable e);
}
