package com.xxelin.whale.core;

import com.xxelin.whale.config.CachedMethodConfig;
import com.xxelin.whale.utils.Null;
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

    private String methodKey;

    private CachedMethodConfig config;

    public RedisTemplateCacher(String methodKey, CachedMethodConfig config) {
        this.methodKey = methodKey;
        this.config = config;
    }

    @Override
    public <T> T load(String cacheKey, SourceBack<T> method) throws Exception {
        if (!RedisHolder.isEnable()) {
            log.error(ERROR_MSG);
            return method.get();
        }
        byte[] redisKey = redisCacheKey(cacheKey, config.getNameSpace());

        RedisTemplate redisTemplate = RedisHolder.getRedisTemplate();
        Object dataFromRedis = redisTemplate.opsForValue().get(redisKey);
        if (dataFromRedis instanceof byte[]) {
            Object deserialize = serializer.deserialize((byte[]) dataFromRedis);
            if (deserialize != null) {
                if (deserialize instanceof Null) {
                    return null;
                }
                return (T) deserialize;
            }
        }
        return sourceBack(redisKey, method);
    }

    private <T> T sourceBack(byte[] redisKey, SourceBack<T> method) throws Exception {
        Object data = method.get();
        if (data == null) {
            if (!config.isCacheNull()) {
                return null;
            }
            data = Null.of();
        }
        byte[] bytes = serializer.serialize(data);
        RedisTemplate redisTemplate = RedisHolder.getRedisTemplate();
        redisTemplate.opsForValue().set(redisKey, bytes, config.getExpire(), config.getTimeUnit());
        return data instanceof Null ? null : (T) data;
    }

    @Override
    public void invalidate(String key) {
        if (!RedisHolder.isEnable()) {
            log.error(ERROR_MSG);
            return;
        }
        byte[] redisKey = redisCacheKey(key, config.getNameSpace());
        RedisHolder.getRedisTemplate().delete(redisKey);
    }

    private byte[] redisCacheKey(String methodKey, String nameSpace) {
        return serializer.serialize(nameSpace + "_" + methodKey);
    }

}
