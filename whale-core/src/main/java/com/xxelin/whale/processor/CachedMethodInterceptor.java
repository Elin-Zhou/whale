package com.xxelin.whale.processor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xxelin.whale.annotation.Cached;
import com.xxelin.whale.utils.FormatUtils;
import com.xxelin.whale.utils.Null;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CachedMethodInterceptor.java , v 0.1 2019-08-01 11:36 ElinZhou Exp $
 */
@Slf4j
public class CachedMethodInterceptor implements MethodInterceptor, InvocationHandler {

    private Object objectProxy;

    private Class<?> originalClass;

    private Map<Method, Cached> cachedMap;

    private ConcurrentHashMap<String, Cache<String, Object>> cache = new ConcurrentHashMap<>(128);


    public CachedMethodInterceptor(Object objectProxy, Map<Method, Cached> cachedMap) {
        this.objectProxy = objectProxy;
        this.cachedMap = cachedMap;
        init();
    }

    private void init() {
        originalClass = ClassUtils.getUserClass(objectProxy);
        if (Proxy.isProxyClass(originalClass) || ClassUtils.isCglibProxyClass(originalClass)) {
            Class<?>[] interfaces = originalClass.getInterfaces();
            if (interfaces.length == 0) {
                throw new IllegalStateException("unsupport cache method!");
            }
            originalClass = interfaces[interfaces.length - 1];
        }
        for (Map.Entry<Method, Cached> entry : cachedMap.entrySet()) {
            String method = FormatUtils.format(entry.getKey());
            Cache<String, Object> cache =
                    Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(10000).build();
            this.cache.put(method, cache);
        }
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return invoke(o, method, objects);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Cached cached = AnnotationUtils.findAnnotation(method, Cached.class);
        if (cached == null) {
            return method.invoke(objectProxy, args);
        }
        String key = FormatUtils.cacheKey(originalClass, method, args);
        if (log.isDebugEnabled()) {
            log.debug("[use cache]{}", key);
        }
        Cache<String, Object> caffeine = cache.get(FormatUtils.format(method));
        Object value = caffeine.getIfPresent(key);
        if (value != null) {
            log.debug("[hit cache]{}", key);
            if (value instanceof Null) {
                return null;
            }
            return value;
        }
        long start = System.currentTimeMillis();
        Object result = method.invoke(objectProxy, args);
        if (log.isDebugEnabled()) {
            log.debug("[miss cache,spend:{}]{}", (System.currentTimeMillis() - start), key);
        }
        if (result != null) {
            caffeine.put(key, result);
        } else if (cached.cacheNull()) {
            caffeine.put(key, Null.of());
        }
        return result;
    }
}
