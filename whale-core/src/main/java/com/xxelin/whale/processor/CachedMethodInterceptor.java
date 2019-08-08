package com.xxelin.whale.processor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xxelin.whale.annotation.Cached;
import com.xxelin.whale.config.CachedMethodConfig;
import com.xxelin.whale.config.GlobalConfig;
import com.xxelin.whale.core.CaffeineCacher;
import com.xxelin.whale.core.LocalCacher;
import com.xxelin.whale.enums.CacheType;
import com.xxelin.whale.utils.FormatUtils;
import com.xxelin.whale.utils.SpelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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

    private ConcurrentHashMap<String, LocalCacher> localCacherMap = new ConcurrentHashMap<>(128);

    private ConcurrentHashMap<String, CachedMethodConfig> configMap = new ConcurrentHashMap<>(128);

    private GlobalConfig globalConfig;

    public CachedMethodInterceptor(Object objectProxy, Map<Method, Cached> cachedMap, GlobalConfig globalConfig) {
        this.objectProxy = objectProxy;
        this.globalConfig = globalConfig;
        init(cachedMap);
    }

    private void init(Map<Method, Cached> cachedMap) {
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
            CachedMethodConfig config = config(entry.getKey(), entry.getValue());
            CacheType type = config.getType();
            if (type == CacheType.LOCAL || type == CacheType.BOTH) {
                Cache<String, Object> cache =
                        Caffeine.newBuilder().expireAfterWrite(config.getLocalExpire(), config.getTimeUnit()).maximumSize(config.getSizeLimit()).build();
                localCacherMap.put(method, new CaffeineCacher(cache));
            }
            configMap.put(method, config);
        }
    }

    private CachedMethodConfig config(Method method, Cached cached) {
        CachedMethodConfig config = new CachedMethodConfig();
        config.setNameSpace(globalConfig.getNamespace());
        config.setId(StringUtils.isNotEmpty(cached.id()) ? cached.id() : null);
        if (cached.expire() == -1 && globalConfig.getExpireSeconds() == null) {
            throw new IllegalStateException("[" + method.getDeclaringClass().getName() + "." + method.getName() + "] " +
                    "must set expire time");
        }
        config.setExpire(cached.expire() == -1 ? globalConfig.getExpireSeconds() : cached.expire());
        config.setTimeUnit(cached.expire() == -1 ? TimeUnit.SECONDS : cached.timeUnit());
        config.setLocalExpire(cached.localExpire() == -1 ? config.getExpire() : cached.localExpire());
        config.setType(cached.type());
        int sizeLimit = cached.sizeLimit();
        if (globalConfig.getMaxSizeLimit() != null && sizeLimit > globalConfig.getMaxSizeLimit()) {
            sizeLimit = globalConfig.getMaxSizeLimit();
        }
        config.setSizeLimit(sizeLimit);
        config.setConsistency(cached.consistency() || globalConfig.isConsistency());
        config.setCacheNull(cached.cacheNull() || globalConfig.isCacheNull());
        return config;
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


        String methodKey = FormatUtils.format(method);
        LocalCacher localCacher = localCacherMap.get(methodKey);
        CachedMethodConfig config = configMap.get(methodKey);
        //解析spel表达式
        if (StringUtils.isNotEmpty(config.getCondition()) && BooleanUtils.isNotTrue(SpelUtils.parse(config.getCondition(),
                Boolean.class, originalClass, method, args))) {
            return method.invoke(objectProxy, args);
        }
        if (log.isDebugEnabled()) {
            log.debug("{} user cache type:{}", FormatUtils.format(originalClass, method), config.getType());
        }
        String id = config.getId();
        String key = StringUtils.isEmpty(id) ?
                FormatUtils.cacheKey(originalClass, method, args) :
                FormatUtils.cacheKey(originalClass, method, SpelUtils.parse(id, String.class, originalClass, method,
                        args));

        if (config.getType() == CacheType.LOCAL || config.getType() == CacheType.BOTH) {
            return localCacher.load(key, () -> method.invoke(objectProxy, args), config);
        }
        return method.invoke(objectProxy, args);
    }
}
