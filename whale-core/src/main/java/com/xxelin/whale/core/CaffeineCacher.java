package com.xxelin.whale.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.xxelin.whale.config.CachedMethodConfig;
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
    @SuppressWarnings("unchecked")
    public <T> T load(String key, SourceBack<T> method) throws Exception {
        boolean hit = true;
        Object result = cache.getIfPresent(key);
        if (result == null) {
            synchronized (CacheLockHolder.getLock(key)) {
                if ((result = cache.getIfPresent(key)) == null) {
                    hit = false;
                    long start = System.currentTimeMillis();
                    result = sourceBack(key, method);
                    MonitorHolder.requestAndMiss(originalClass, name, System.currentTimeMillis() - start);
                }
            }
        } else {
            log.debug("[hit cache]{}", key);
            MonitorHolder.requestAndHit(originalClass, name);
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
        if (log.isDebugEnabled()) {
            log.debug("[miss cache,spend:{}ms]{}", (System.currentTimeMillis() - start), key);
        }
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
