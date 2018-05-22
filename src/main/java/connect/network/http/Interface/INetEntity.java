package connect.network.http.Interface;

/**
 * Created by No.9 on 2018/2/22.
 * @author yyz
 */
public interface INetEntity {

    /**
     * post 请求
     * @return
     */
    byte[] postData();

    /**
     * get 请求
     * @param header get 请求要发送的内容
     */
//    void getConnect(Map<String, String> header);
}
