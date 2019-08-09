package com.xxelin.whale.processor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.xxelin.whale.annotation.Cached;
import com.xxelin.whale.config.CachedMethodConfig;
import com.xxelin.whale.config.GlobalConfig;
import com.xxelin.whale.core.CacheAdvanceProxy;
import com.xxelin.whale.core.Cacher;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private ConcurrentHashMap<String, Method> methodMap = new ConcurrentHashMap<>(128);

    private GlobalConfig globalConfig;

    private CacheAdvanceProxy cacheAdvanceProxy;

    public CachedMethodInterceptor(Object objectProxy, Map<Method, Cached> cachedMap, GlobalConfig globalConfig) {
        this.objectProxy = objectProxy;
        this.globalConfig = globalConfig;
        init(cachedMap);
        cacheAdvanceProxy = new CacheAdvanceProxy(this);

        log.debug("{} create cache proxy", originalClass.getName());
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
            Cached cached = entry.getValue();
            String method = StringUtils.isNotEmpty(cached.value()) ? cached.value() :
                    FormatUtils.format(entry.getKey());
            CachedMethodConfig config = config(entry.getKey(), cached);
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
        config.setNameSpace(StringUtils.isNotEmpty(cached.nameSpace()) ? cached.nameSpace() : globalConfig.getNamespace());
        config.setId(StringUtils.isNotEmpty(cached.idExpress()) ? cached.idExpress() : null);
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
        config.setCondition(cached.condition());
        return config;
    }

    public Optional<LocalCacher> getLocalCacher(String methodKey) {
        return Optional.ofNullable(localCacherMap.get(methodKey));
    }

    public List<Cacher> getCacher(String methodKey) {
        return Lists.newArrayList(localCacherMap.get(methodKey));
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return invoke(o, method, objects);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (cacheAdvanceProxy.isAdvanceMethod(method)) {
            return cacheAdvanceProxy.invokeAdvance(method, args);
        }

        Cached cached = AnnotationUtils.findAnnotation(method, Cached.class);
        if (cached == null) {
            return method.invoke(objectProxy, args);
        }
        String methodKey = StringUtils.isNotEmpty(cached.value()) ? cached.value() : FormatUtils.format(method);
        methodMap.putIfAbsent(methodKey, method);
        LocalCacher localCacher = localCacherMap.get(methodKey);
        CachedMethodConfig config = configMap.get(methodKey);
        //解析spel表达式
        if (StringUtils.isNotEmpty(config.getCondition()) && BooleanUtils.isNotTrue(SpelUtils.parse(config.getCondition(),
                Boolean.class, originalClass, method, args))) {
            return method.invoke(objectProxy, args);
        }
        String id = config.getId();
        String key = StringUtils.isEmpty(id) ?
                FormatUtils.cacheKey(originalClass, methodKey, args) :
                FormatUtils.cacheKey(originalClass, methodKey, SpelUtils.parse(id, String.class, originalClass, method,
                        args));
        if (log.isDebugEnabled()) {
            log.debug("{} user cache type:{}", key, config.getType());
        }
        if (config.getType() == CacheType.LOCAL || config.getType() == CacheType.BOTH) {
            return localCacher.load(key, () -> method.invoke(objectProxy, args), config);
        }
        return method.invoke(objectProxy, args);
    }

    public String cacheKey(String methodKey, Object[] args) {
        CachedMethodConfig config = configMap.get(methodKey);
        String id = config.getId();
        return StringUtils.isEmpty(id) ?
                FormatUtils.cacheKey(originalClass, methodKey, args) :
                FormatUtils.cacheKey(originalClass, methodKey, SpelUtils.parse(id, String.class, originalClass,
                        methodMap.get(methodKey), args));
    }

    public String cacheKey(String methodKey, String id) {
        return FormatUtils.cacheKey(originalClass, methodKey, id);
    }
}
