package com.xxelin.whale.annotation;

import com.xxelin.whale.enums.CacheType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: Cached.java , v 0.1 2019-07-31 11:44 ElinZhou Exp $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface Cached {

    String nameSpace() default "";

    /**
     * 缓存id，用来作为缓存的key，根据此字段查询缓存
     * 不设置此值时将所有参数通过json序列化作为key
     * 如果需要使用自定义key，请使用SpEL表达式
     *
     * @return
     */
    String idExpress() default "";

    long expire() default -1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long localExpire() default -1;

    CacheType type() default CacheType.LOCAL;

    int sizeLimit() default Integer.MAX_VALUE;

    boolean consistency() default false;

    boolean cacheNull() default false;

    /**
     * 使用SpEL表达式，如果表达式返回true，则使用缓存（如果命中）；如果表达式返回false，就直接回源数据
     *
     * @return
     */
    String condition() default "";

    /**
     * 同name字段
     * @return
     */
    String value() default "";

    /**
     * 方法名称
     * 如果不指定此值，则无法手动失效缓存
     *
     * @return
     */
    String name() default "";


    /**
     * 是否启用缓存
     *
     * @return
     */
    boolean enable() default true;
}
