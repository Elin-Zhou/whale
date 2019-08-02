package com.xxelin.whale.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.xxelin.whale.config.CachedMethodConfig;
import com.xxelin.whale.utils.Null;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CaffeineCacher.java , v 0.1 2019-08-02 14:41 ElinZhou Exp $
 */
@Slf4j
public class CaffeineCacher implements LocalCacher {

    private static final LongAdder HIT_TIMES = new LongAdder();

    private static final LongAdder MISS_TIMES = new LongAdder();

    private Cache<String, Object> cache;

    public CaffeineCacher(Cache<String, Object> cache) {
        this.cache = cache;
    }

    @Override
    public long hitTimes() {
        return HIT_TIMES.longValue();
    }

    @Override
    public long missTimes() {
        return MISS_TIMES.longValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T load(String key, SourceBack<T> method, CachedMethodConfig cachedMethodConfig) throws Exception {

        Object value = cache.getIfPresent(key);
        if (value instanceof Null) {
            log.debug("[hit cache]{}", key);
            HIT_TIMES.increment();
            return null;
        } else if (value != null) {
            log.debug("[hit cache]{}", key);
            HIT_TIMES.increment();
            return (T) value;
        }
        MISS_TIMES.increment();
        long start = System.currentTimeMillis();
        T result = method.get();
        if (log.isDebugEnabled()) {
            log.debug("[miss cache,spend:{}ms]{}", (System.currentTimeMillis() - start), key);
        }
        if (result != null || cachedMethodConfig.isCacheNull()) {
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
