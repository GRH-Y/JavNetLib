package connect.network.http.joggle;

import connect.network.base.RequestMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 网络请求注解
 * Created by No.9 on 2018/2/22.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AXRequest {

    /**
     * 请求类型（POST 或者 GET）
     *
     * @return
     */
    RequestMode requestMode() default RequestMode.GET;

    /**
     * 请求的地址
     *
     * @return
     */
    String url();

    /**
     * 请求回调结果的方法名JavKeep
     * 方法参数必须是（XRequest request, XResponse response,Exception ex）
     *
     * @return
     */
    String callBackMethod() default "";

    /**
     * 本次请求过程状态回调方法名
     * 方法参数必须是 (int process, int maxProcess, boolean isOver)
     *
     * @return
     */
    String processMethod() default "";

    /**
     * resultType 字段是解析成当前结果为制定的类型
     * （byte[].class 为不进行任何转换，直接返回接收到的数据）
     * （其他的class 则进行解析成指定的类型）
     *
     * @return
     */
    Class resultType();

}
