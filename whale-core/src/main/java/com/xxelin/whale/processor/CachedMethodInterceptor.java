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
import com.xxelin.whale.core.MonitorHolder;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

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

    private Cached classCached;

    public CachedMethodInterceptor(Object objectProxy, Map<Method, Cached> cachedMap, Cached classCached,
                                   GlobalConfig globalConfig) {
        this.objectProxy = objectProxy;
        this.globalConfig = globalConfig;
        this.classCached = classCached;
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
            String method = methodKey(entry.getKey(), cached);
            CachedMethodConfig config = newConfig(entry.getKey(), cached);
            CacheType type = config.getType();
            if (type == CacheType.LOCAL || type == CacheType.BOTH) {
                Cache<String, Object> cache =
                        Caffeine.newBuilder().expireAfterWrite(config.getLocalExpire(), config.getTimeUnit()).maximumSize(config.getSizeLimit()).build();
                localCacherMap.put(method, new CaffeineCacher(cache, originalClass, method));
            }
            configMap.put(method, config);
            MonitorHolder.init(originalClass, method);
        }
    }


    private CachedMethodConfig newConfig(Method method, Cached methodCached) {

        List<Cached> cacheConfigChain = new ArrayList<>(2);
        if (classCached != null) {
            cacheConfigChain.add(classCached);
        }
        if (methodCached != null) {
            cacheConfigChain.add(methodCached);
        }


        CachedMethodConfig config = new CachedMethodConfig();
        config.setNameSpace(globalConfig.getNamespace());
        config.setExpire(globalConfig.getExpireSeconds());
        config.setConsistency(globalConfig.isConsistency());
        config.setCacheNull(globalConfig.isCacheNull());
        config.setLocalExpire(config.getExpire());
        config.setSizeLimit(globalConfig.getMaxSizeLimit());


        Predicate<Long> notNegativeLongCheck = p -> p > 0;
        Predicate<Integer> notNegativeIntCheck = p -> p > 0;
        for (Cached cached : cacheConfigChain) {

            config.setNameSpace(firstValidValue(cached.nameSpace(), config.getNameSpace(), StringUtils::isNotEmpty));
            config.setId(firstValidValue(cached.idExpress(), config.getId(), StringUtils::isNotEmpty));
            config.setExpire(firstValidValue(cached.expire(), config.getExpire(), notNegativeLongCheck));
            config.setTimeUnit(firstValidValue(cached.timeUnit(), config.getTimeUnit(), Objects::nonNull));
            config.setLocalExpire(firstValidValue(cached.localExpire(), config.getLocalExpire(), notNegativeLongCheck));
            config.setType(firstValidValue(cached.type(), config.getType(), Objects::nonNull));
            config.setSizeLimit(firstValidValue(cached.sizeLimit(), config.getSizeLimit(), notNegativeIntCheck));
            config.setConsistency(firstValidValue(cached.consistency(), config.isConsistency(), p -> true));
            config.setCacheNull(firstValidValue(cached.cacheNull(), config.isCacheNull(), p -> true));

            config.setCondition(firstValidValue(cached.condition(), config.getCondition(), StringUtils::isNotEmpty));

        }

        if (config.getExpire() == null || config.getExpire() == -1) {
            throw new IllegalStateException("[" + method.getDeclaringClass().getName() + "." + method.getName() + "] " +
                    "must set expire time");
        }
        if (globalConfig.getMaxSizeLimit() != null && config.getSizeLimit() > globalConfig.getMaxSizeLimit()) {
            config.setSizeLimit(globalConfig.getMaxSizeLimit());
        }
        config.setLocalExpire(firstValidValue(config.getLocalExpire(), config.getExpire(), notNegativeLongCheck));
        return config;
    }

    private <T> T firstValidValue(T highPriority, T lowPriority, Predicate<T> validCheck) {
        if (highPriority != null && validCheck.test(highPriority)) {
            return highPriority;
        }
        if (lowPriority != null && validCheck.test(lowPriority)) {
            return lowPriority;
        }
        return null;
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
        String methodKey = methodKey(method, cached);
        CachedMethodConfig config = configMap.get(methodKey);
        if (config == null) {
            return method.invoke(objectProxy, args);
        }

        methodMap.putIfAbsent(methodKey, method);
        LocalCacher localCacher = localCacherMap.get(methodKey);
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

    private String methodKey(Method method, Cached cached) {
        if (cached == null) {
            return FormatUtils.format(method);
        }
        if (StringUtils.isNotEmpty(cached.value())) {
            return cached.value();
        } else if (StringUtils.isNotEmpty(cached.name())) {
            return cached.name();
        }
        return FormatUtils.format(method);
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
