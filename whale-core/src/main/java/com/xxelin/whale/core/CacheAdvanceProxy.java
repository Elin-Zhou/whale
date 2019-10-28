package com.xxelin.whale.core;

import com.google.common.collect.ImmutableMap;
import com.xxelin.whale.core.cacher.LocalCacher;
import com.xxelin.whale.message.redis.RedisPublisher;
import com.xxelin.whale.message.redis.RedisTopic;
import com.xxelin.whale.message.redis.entity.InvalideAllMessage;
import com.xxelin.whale.message.redis.entity.InvalideMessage;
import com.xxelin.whale.processor.CachedMethodInterceptor;
import com.xxelin.whale.utils.BeanFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheAdvanceProxy.java , v 0.1 2019-08-09 14:05 ElinZhou Exp $
 */
@Slf4j
public class CacheAdvanceProxy {

    private String delgateBeanName;

    private final Map<String, Method> methodMap;

    private Invoker invoker;

    private CachedMethodInterceptor interceptor;

    public CacheAdvanceProxy(String delgateBeanName, CachedMethodInterceptor interceptor) {
        Method[] methods = CacheAdvance.class.getDeclaredMethods();
        Map<String, Method> map = new HashMap<>(methods.length);
        for (Method method : methods) {
            map.put(method.getName(), method);
        }
        methodMap = ImmutableMap.copyOf(map);
        invoker = new Invoker();
        this.interceptor = interceptor;
        this.delgateBeanName = delgateBeanName;
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
            InvalideAllMessage message = new InvalideAllMessage();
            message.setMethodKey(methodKey);
            message.setBeanName(delgateBeanName);
            BeanFactory.getBean(RedisPublisher.class).ifPresent(p -> p.publish(RedisTopic.INVALIDATE_ALL, message));
        }

        @Override
        public void invalidate$Proxy(String methodKey, Object... params) {
            String cacheKey = interceptor.cacheKey(methodKey, params);
            log.debug("invoke CacheAdvance.invalidate$Proxy,value:{},cacheKey:{}", methodKey, cacheKey);
            interceptor.getCacher(methodKey).stream().filter(Objects::nonNull).forEach(c -> c.invalidate(cacheKey));
            invalidate(methodKey, cacheKey);
        }

        @Override
        public void invalidateWithId$Proxy(String methodKey, String id) {
            String cacheKey = interceptor.cacheKey(methodKey, id);
            log.debug("invoke CacheAdvance.invalidateWithId$Proxy,value:{},cacheKey:{}", methodKey, cacheKey);
            interceptor.getCacher(methodKey).stream().filter(Objects::nonNull).forEach(c -> c.invalidate(cacheKey));
            invalidate(methodKey, cacheKey);
        }

        private void invalidate(String methodKey, String cacheKey) {
            InvalideMessage message = new InvalideMessage();
            message.setMethodKey(methodKey);
            message.setCacheKey(cacheKey);
            message.setBeanName(delgateBeanName);
            BeanFactory.getBean(RedisPublisher.class).ifPresent(p -> p.publish(RedisTopic.INVALIDATE, message));
        }
    }


}
