package connect.network.http.joggle;

import connect.network.http.JavHttpConnect;

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
@KeepPrototype
public @interface ARequest {

    /**
     * 请求类型（POST 或者 GET）
     *
     * @return
     */
    Class requestType();

    /**
     * 请求的地址
     *
     * @return
     */
    String url();

    /**
     * 任务tag (区别任务)
     * @return
     */
    int taskTag() default JavHttpConnect.DEFAULT_TASK_TAG;


    /**
     * 本次请求成功回调结果的方法名
     *
     * @return
     */
    String successMethod() default "";

    /**
     * 本次请求失败回调结果的方法名
     *
     * @return
     */
    String errorMethod() default "";

    /**
     * resultType 字段是解析成当前结果为制定的类型
     * （byte[].class 为不进行任何转换，直接返回接收到的数据）
     * （其他的class 则进行解析成指定的类型）
     *
     * @return
     */
    Class resultType();

    /**
     * 如果 resultType 是String.class ,则savePath 注解字段是解析成结果保存为文件
     *
     * @return
     */
    String savePath() default "";
}
