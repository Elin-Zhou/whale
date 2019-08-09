package com.xxelin.whale.core;

import com.google.common.collect.ImmutableMap;
import com.xxelin.whale.processor.CachedMethodInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheAdvanceProxy.java , v 0.1 2019-08-09 14:05 ElinZhou Exp $
 */
@Slf4j
public class CacheAdvanceProxy {

    private final Map<String, Method> methodMap;

    private Invoker invoker;

    private CachedMethodInterceptor interceptor;

    public CacheAdvanceProxy(CachedMethodInterceptor interceptor) {
        Method[] methods = CacheAdvance.class.getDeclaredMethods();
        Map<String, Method> map = new HashMap<>(methods.length);
        for (Method method : methods) {
            map.put(method.getName(), method);
        }
        methodMap = ImmutableMap.copyOf(map);
        invoker = new Invoker();
        this.interceptor = interceptor;
    }

    public boolean isAdvanceMethod(Method method) {
        return methodMap.containsKey(method.getName());
    }

    public Object invokeAdvance(Method method, Object[] args) {
        Method proxyMethod = methodMap.get(method.getName());
        try {
            return proxyMethod.invoke(invoker, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("invoke {} error", method.getName(), e);
            return null;
        }
    }

    class Invoker implements CacheAdvance {

        @Override
        public void invalidateAll$Proxy(String methodKey) {
            log.debug("invoke CacheAdvance.invalidateAll$Proxy,value:{}", methodKey);
            interceptor.getLocalCacher(methodKey).ifPresent(LocalCacher::invalidateAll);
        }

        @Override
        public void invalidate$Proxy(String methodKey, Object... params) {
            String cacheKey = interceptor.cacheKey(methodKey, params);
            log.debug("invoke CacheAdvance.invalidate$Proxy,value:{},cacheKey:{}", methodKey, cacheKey);
            interceptor.getCacher(methodKey).forEach(c -> c.invalidate(cacheKey));
        }

        @Override
        public void invalidateWithId$Proxy(String methodKey, String id) {
            String cacheKey = interceptor.cacheKey(methodKey, id);
            log.debug("invoke CacheAdvance.invalidateWithId$Proxy,value:{},cacheKey:{}", methodKey, cacheKey);
            interceptor.getCacher(methodKey).forEach(c -> c.invalidate(cacheKey));
        }
    }


}
