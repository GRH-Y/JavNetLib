package connect.network.http.joggle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * HEAD 方式请求 (类似于get请求，只不过返回的响应中没有具体的内容，用于获取报头)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HEAD {
}
