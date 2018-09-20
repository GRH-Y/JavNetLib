package connect.network.http.joggle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CONNECT 方式请求  HTTP/1.1协议中预留给能够将连接改为管道方式的代理服务器。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CONNECT {
}
