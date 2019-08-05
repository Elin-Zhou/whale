package com.xxelin.core.test;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheTest.java , v 0.1 2019-08-02 17:53 ElinZhou Exp $
 */
public class CacheTest {

    @Test
    public void cacheTest() throws InterruptedException {

        Cache<String, Long> cache = Caffeine.newBuilder().build();

        String key = "key";

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        for (int i = 2; i > 0; i--) {
            executorService.submit(() -> {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Long time = cache.get(key, k -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(RandomUtils.nextInt(500, 5000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return System.currentTimeMillis();
                });
                System.out.println("time:" + time);
            });
            countDownLatch.countDown();
        }

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void weak() {

        String a = "abc";
        String b = new String("abc");

        WeakReference<String> ar = new WeakReference<>(a);
        WeakReference<String> br = new WeakReference<>(b);

        System.out.println(ar == br);
        System.out.println(ar.equals(br));

    }

}
