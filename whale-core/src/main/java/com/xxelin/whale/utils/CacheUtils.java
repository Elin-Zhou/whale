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
     * 无法在缓存所在的对象中使用当前方法
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
     * 失效该对象中指定方法中的所有缓存
     * 仅支持失效本地缓存
     * 无法在缓存所在的对象中使用当前方法
     *
     * @param bean
     * @param methodKey
     */
    public static void invalidate(Object bean, String methodKey, Object... params) {
        if (isCacheAdvance(bean)) {
            return;
        }
        CacheAdvance cacheAdvance = (CacheAdvance) bean;
        cacheAdvance.invalidate$Proxy(methodKey, params);
    }

    /**
     * 失效该对象中指定方法中的所有缓存
     * 仅支持失效本地缓存
     * 无法在缓存所在的对象中使用当前方法
     *
     * @param bean
     * @param methodKey
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
