package com.xxelin.whale.utils;

import com.xxelin.whale.core.CacheAdvance;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheUtils.java , v 0.1 2019-08-09 10:20 ElinZhou Exp $
 */
public class CacheUtils {

    private CacheUtils() {
        throw new IllegalStateException("cant instance");
    }

    /**
     * 失效该对象中指定方法中的所有缓存
     * 仅支持失效本地缓存
     *
     * @param bean
     * @param methodKey
     */
    public static void invalidateAll(Object bean, String methodKey) {

        if (isCacheAdvance(bean)) {
            return;
        }
        CacheAdvance cacheAdvance = (CacheAdvance) bean;
        cacheAdvance.invalidateAll$Proxy(methodKey);
    }

    /**
     * 失效该对象中指定方法中的指定缓存
     * 需要失效的缓存通过方法入参指定，要求与原缓存方法中的入参一致
     *
     * @param bean
     * @param methodKey
     * @param params 缓存时使用的参数
     */
    public static void invalidateWithParams(Object bean, String methodKey, Object... params) {
        if (isCacheAdvance(bean)) {
            return;
        }
        CacheAdvance cacheAdvance = (CacheAdvance) bean;
        cacheAdvance.invalidate$Proxy(methodKey, params);
    }

    /**
     * 失效该对象中指定方法中的指定缓存
     * 需要失效的缓存通过方法入参指定，要求与原缓存方法中的入参一致
     *
     * @param bean
     * @param methodKey
     * @param id 缓存id,通过idExpress计算得到
     */
    public static void invalidateWithId(Object bean, String methodKey, String id) {
        if (isCacheAdvance(bean)) {
            return;
        }
        CacheAdvance cacheAdvance = (CacheAdvance) bean;
        cacheAdvance.invalidateWithId$Proxy(methodKey, id);
    }

    private static boolean isCacheAdvance(Object bean) {
        if (!ClassUtils.isCglibProxy(bean) && !Proxy.isProxyClass(bean.getClass())) {
            return true;
        }
        return !(bean instanceof CacheAdvance);
    }

}
