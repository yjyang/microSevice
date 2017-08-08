package com.fd.microSevice.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fd.microSevice.em.Pl;

/**
 * 开放级别
 * 
 * @author 符冬
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
public @interface RestApi {
	/**
	 * 访问级别 默认公开访问
	 * 
	 */
	Pl value() default Pl.PUBLIC;
}
