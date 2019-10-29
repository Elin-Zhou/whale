package com.xxelin.whale.utils;

import com.xxelin.whale.core.RedisHolder;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisLockUtils.java , v 0.1 2019-09-09 14:02 ElinZhou Exp $
 */
public class RedisLockUtils {
    private static final String COMPARE_AND_DELETE =
            "if redis.call('get',KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call('del',KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";

    private RedisLockUtils() {
        throw new UnsupportedOperationException();
    }

    public static RedisLock getLock(final String key, final int expireSeconds, int maxRetryTimes,
                                    long retryIntervalTimeMillis) {
        final String value = UUID.randomUUID().toString();

        RedisTemplate redisTemplate = RedisHolder.getRedisTemplate();

        int maxTimes = maxRetryTimes + 1;
        for (int i = 0; i < maxTimes; i++) {

            String status = (String) redisTemplate.execute((RedisCallback<String>) connection -> {
                Jedis jedis = (Jedis) connection.getNativeConnection();
                return jedis.set(key, value, "nx", "ex", expireSeconds);
            });

            if ("OK".equals(status)) {
                return new RedisLockInner(redisTemplate, key, value);
            }

            if (retryIntervalTimeMillis > 0) {
                try {
                    Thread.sleep(retryIntervalTimeMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }

        return null;
    }

    private static class RedisLockInner implements RedisLock {

        private RedisTemplate redisTemplate;
        private String key;
        private String expectedValue;
        private long threadId;

        RedisLockInner(RedisTemplate redisTemplate, String key, String expectedValue) {
            this.redisTemplate = redisTemplate;
            this.key = key;
            this.expectedValue = expectedValue;
            threadId = Thread.currentThread().getId();
        }

        /**
         * 释放redis分布式锁
         */
        @Override
        @SuppressWarnings("unchecked")
        public void unlock() {
            if (Thread.currentThread().getId() != threadId) {
                throw new IllegalMonitorStateException();
            }
            List<String> keys = Collections.singletonList(key);
            redisTemplate.execute(new DefaultRedisScript<>(COMPARE_AND_DELETE, Long.class), keys, expectedValue);
        }

        @Override
        public void close() {
            this.unlock();
        }
    }
}
