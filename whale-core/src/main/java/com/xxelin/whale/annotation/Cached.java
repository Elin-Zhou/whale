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
@Target({ElementType.METHOD})
@Inherited
public @interface Cached {

    String nameSpace() default "";

    String name() default "";

    long expire() default -1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    long localExpire() default -1;

    CacheType type() default CacheType.LOCAL;

    int sizeLimit() default Integer.MAX_VALUE;

    boolean consistency() default false;

    boolean cacheNull() default false;

}
