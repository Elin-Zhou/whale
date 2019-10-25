package com.xxelin.whale.core;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisHolder.java , v 0.1 2019-09-06 16:41 ElinZhou Exp $
 */
public class RedisHolder {

    private static boolean enable = false;

    private static RedisTemplate redisTemplate;

    public RedisHolder(RedisTemplate redisTemplate) {
        if (redisTemplate != null) {
            enable = true;
            this.redisTemplate = redisTemplate;
        }
    }

    public static void setEnable(boolean enable) {
        RedisHolder.enable = enable;
    }

    public static boolean isEnable() {
        return enable;
    }

    public static RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }
}
