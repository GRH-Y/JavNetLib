package connect.network.http.joggle;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * 保留原型,不被混淆
 */
@Retention(CLASS)
@Target({PACKAGE,TYPE,ANNOTATION_TYPE,CONSTRUCTOR,METHOD,FIELD})
public @interface KeepPrototype {
}
