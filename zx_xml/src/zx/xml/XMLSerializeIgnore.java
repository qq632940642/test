package zx.xml;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XMLSerializeIgnore {
	/**
	 * 忽略,不会序列化和反序列化
	 * */
	boolean ignore() default true;
}
