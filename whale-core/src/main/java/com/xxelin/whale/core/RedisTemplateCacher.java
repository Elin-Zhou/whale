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

    @Override
    public <T> T load(String key, SourceBack<T> method, CachedMethodConfig config) throws Exception {
        if (!RedisHolder.isEnable()) {
            log.error(ERROR_MSG);
            return method.get();
        }
        String nameSpace = config.getNameSpace();
        byte[] redisKey = serializer.serialize(nameSpace + "_" + key);

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
        Object data = method.get();
        if (data == null) {
            if (!config.isCacheNull()) {
                return null;
            }
            data = Null.of();
        }

        byte[] bytes = serializer.serialize(data);
        redisTemplate.opsForValue().set(redisKey, bytes, config.getExpire(), config.getTimeUnit());
        return (T) data;
    }

    @Override
    public void invalidate(String key) {
        if (!RedisHolder.isEnable()) {
            log.error(ERROR_MSG);
            return;
        }
        RedisHolder.getRedisTemplate().delete(key);
    }

}
