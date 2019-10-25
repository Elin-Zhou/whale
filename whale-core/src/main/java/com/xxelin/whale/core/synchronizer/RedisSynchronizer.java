package com.xxelin.whale.core.synchronizer;

import com.xxelin.whale.core.RedisHolder;
import com.xxelin.whale.core.cacher.LocalCacher;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisSynchronizer.java , v 0.1 2019-10-14 19:42 ElinZhou Exp $
 */
@Slf4j
public class RedisSynchronizer {

    private static final int CHECK_INTERVAL_MILLISECONDS = 500;

    private static int failTimes = 0;

    private static List<LocalCacher> localCachers = new ArrayList<>(128);

    public static void init() {
        new Thread(RedisSynchronizer::run).start();
    }

    public static void registeLocalCacher(LocalCacher localCacher) {
        localCachers.add(localCacher);
    }

    /**
     * 失效所有已经注册的本地缓存中的值
     */
    public static void invalidateAll() {
        localCachers.parallelStream().forEach(LocalCacher::invalidateAll);
    }


    public static void run() {
        while (true) {
            try {
                RedisHolder.getRedisTemplate().opsForValue().get("TEST");
                failTimes = 0;
                RedisHolder.setEnable(true);
                TimeUnit.MILLISECONDS.sleep(CHECK_INTERVAL_MILLISECONDS);
            } catch (Exception e) {
                log.error("redis cycle check error", e);
                failTimes++;
                if (failTimes == 3) {
                    log.error("redis check fail over threshold");
                    RedisHolder.setEnable(false);
                    invalidateAll();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(CHECK_INTERVAL_MILLISECONDS * 3);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }


}
