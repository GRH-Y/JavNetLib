package connect.network.http.joggle;

import connect.network.base.RequestMode;
import connect.network.http.RequestEntity;

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
public @interface ARequest {

    /**
     * 是否启用HttpTaskConfig配置的基本的url
     *
     * @return
     */
    boolean disableBaseUrl() default false;

    /**
     * 是否是独立任务，如果是则单独开启线程处理
     * @return
     */
    boolean isIndependentTask() default false;

    /**
     * 请求类型（POST 或者 GET）
     *
     * @return
     */
    RequestMode requestMode();

    /**
     * 请求的地址
     *
     * @return
     */
    String url();

    /**
     * 任务tag (区别任务)
     *
     * @return
     */
    int taskTag() default RequestEntity.DEFAULT_TASK_TAG;


    /**
     * 本次请求成功回调结果的方法名
     * 方法参数必须是（对应resultType字段类型）
     *
     * @return
     */
    String successMethod() default "";

    /**
     * 本次请求失败回调结果的方法名
     * 方法参数必须是（RequestEntity tool）
     *
     * @return
     */
    String errorMethod() default "";

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
