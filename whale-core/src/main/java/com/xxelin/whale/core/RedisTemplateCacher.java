package com.xxelin.whale.core;

import com.xxelin.whale.config.CachedMethodConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisTemplateCacher.java , v 0.1 2019-09-06 16:52 ElinZhou Exp $
 */
@Slf4j
@SuppressWarnings("unchecked")
public class RedisTemplateCacher implements RemoteCacher {

    private static final String ERROR_MSG = "无法找到RedisTemplate，远程缓存无法使用，请检查！";

    @Override
    public <T> T load(String key, SourceBack<T> method, CachedMethodConfig config) throws Exception {
        return method.get();
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
