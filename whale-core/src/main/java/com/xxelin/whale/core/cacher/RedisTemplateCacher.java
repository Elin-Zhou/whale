package com.xxelin.whale.core.cacher;

import com.xxelin.whale.config.CachedMethodConfig;
import com.xxelin.whale.core.MonitorHolder;
import com.xxelin.whale.core.RedisHolder;
import com.xxelin.whale.core.SourceBack;
import com.xxelin.whale.utils.Null;
import com.xxelin.whale.utils.RedisLock;
import com.xxelin.whale.utils.RedisLockUtils;
import com.xxelin.whale.utils.serialize.KryoSerializer;
import com.xxelin.whale.utils.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisTemplateCacher.java , v 0.1 2019-09-06 16:52 ElinZhou Exp $
 */
@Slf4j
@SuppressWarnings("unchecked")
public class RedisTemplateCacher implements RemoteCacher {

    private static final String ERROR_MSG = "无法找到RedisTemplate，远程缓存无法使用，请检查！";

    private Serializer serializer = new KryoSerializer();

    private Class<?> originalClass;

    private String methodKey;

    private CachedMethodConfig config;

    private ClassLoader returnTypeClassLoader;

    public RedisTemplateCacher(Class<?> originalClass, String methodKey, CachedMethodConfig config,
                               ClassLoader classLoader) {
        this.originalClass = originalClass;
        this.methodKey = methodKey;
        this.config = config;
        this.returnTypeClassLoader = classLoader;
    }

    @Override
    public String cacheName() {
        return "REDIS";
    }

    @Override
    public <T> T load(String cacheKey, SourceBack<T> method) throws Exception {
        if (!RedisHolder.isEnable()) {
            log.error(ERROR_MSG);
            return method.get();
        }
        byte[] redisKey = redisCacheKey(cacheKey);

        Object data = loadCache(redisKey);
        if (data == null) {
            data = sourceBack(cacheKey, redisKey, method);
        } else {
            log.debug("[hit redis cache]{}", cacheKey);
            MonitorHolder.requestAndHit(originalClass, methodKey, this);
        }
        return data instanceof Null ? null : (T) data;
    }



    private Object loadCache(byte[] redisKey) {
        RedisTemplate redisTemplate = RedisHolder.getRedisTemplate();
        Object dataFromRedis = redisTemplate.opsForValue().get(redisKey);
        if (dataFromRedis instanceof byte[]) {
            Object deserialize = serializer.deserialize((byte[]) dataFromRedis, returnTypeClassLoader);
            if (deserialize == null) {
                return null;
            }
            return deserialize;
        }
        return null;
    }

    private <T> Object sourceBack(String cacheKey, byte[] redisKey, SourceBack<T> method) throws Exception {

        try (RedisLock lock = RedisLockUtils.getLock(lockKey(), 10, 10, 500)) {
            boolean cache = true;
            if (lock == null && config.isConsistency()) {
                //获取分布式锁失败,并且有一致性需求，只回源，不缓存
                cache = false;
            }
            //双重锁检查
            Object temp = loadCache(redisKey);
            if (temp != null) {
                log.debug("[hit redis cache]{}", cacheKey);
                MonitorHolder.requestAndHit(originalClass, methodKey, this);
                return temp;
            }
            long start = System.currentTimeMillis();
            T data = method.get();
            long spend = System.currentTimeMillis() - start;
            log.debug("[miss redis cache,spend:{}ms]{}", spend, cacheKey);
            MonitorHolder.requestAndMiss(originalClass, methodKey, this, spend);

            if (cache) {
                if (data == null && !config.isCacheNull()) {
                    return null;
                }
                byte[] bytes = serializer.serialize(data == null ? Null.of() : data);
                RedisTemplate redisTemplate = RedisHolder.getRedisTemplate();
                redisTemplate.opsForValue().set(redisKey, bytes, config.getExpire(), config.getTimeUnit());
            }
            return data;
        }

    }

    @Override
    public void invalidate(String key) {
        if (!RedisHolder.isEnable()) {
            log.error(ERROR_MSG);
            return;
        }
        byte[] redisKey = redisCacheKey(key);
        RedisHolder.getRedisTemplate().delete(redisKey);
    }

    private byte[] redisCacheKey(String cacheKey) {
        return serializer.serialize(config.getNameSpace() + "_" + cacheKey);
    }

    private String lockKey() {
        return config.getNameSpace() + "_" + methodKey;
    }

}
