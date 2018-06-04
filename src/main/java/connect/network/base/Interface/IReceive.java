package connect.network.base.Interface;

/**
 * 接收
 * @author yyz
 */
public interface IReceive {

    /**
     * 设置接收者
     * @param receive 接收者实体类
     * @param receiveMethodName 接收实体类的方法
     */
    void setReceive(Object receive, String receiveMethodName);

}
