package com.xxelin.whale.core.cacher;

import com.github.benmanes.caffeine.cache.Cache;
import com.xxelin.whale.config.CachedMethodConfig;
import com.xxelin.whale.core.MonitorHolder;
import com.xxelin.whale.core.PageHelperHolder;
import com.xxelin.whale.core.SourceBack;
import com.xxelin.whale.utils.CacheLockHolder;
import com.xxelin.whale.utils.Null;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CaffeineCacher.java , v 0.1 2019-08-02 14:41 ElinZhou Exp $
 */
@Slf4j
@AllArgsConstructor
public class CaffeineCacher implements LocalCacher {

    private Cache<String, Object> cache;

    private Class<?> originalClass;

    private String name;

    private CachedMethodConfig config;

    @Override
    public String cacheName() {
        return "CAFFEINE";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T load(String key, SourceBack<T> method) throws Exception {
        boolean hit = true;
        Object result = cache.getIfPresent(key);
        if (result == null) {
            synchronized (CacheLockHolder.getLock(key)) {
                if ((result = cache.getIfPresent(key)) == null) {
                    hit = false;
                    result = sourceBack(key, method);
                }
            }
        } else {
            log.debug("[hit caffeine cache]{}", key);
            MonitorHolder.requestAndHit(originalClass, name, this);
        }

        if (hit) {
            PageHelperHolder.clear();
        }

        if (result instanceof Null) {
            return null;
        } else if (result != null) {
            return (T) result;
        }
        return null;
    }

    private <T> T sourceBack(String key, SourceBack<T> method) throws Exception {
        long start = System.currentTimeMillis();
        T result = method.get();
        long spend = System.currentTimeMillis() - start;
        log.debug("[miss caffeine cache,spend:{}ms]{}", spend, key);
        MonitorHolder.requestAndMiss(originalClass, name, this, spend);
        if (result != null || config.isCacheNull()) {
            if (result != null) {
                cache.put(key, result);
            } else {
                cache.put(key, Null.of());
            }
        }
        return result;
    }

    @Override
    public void invalidate(String key) {
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }
}
