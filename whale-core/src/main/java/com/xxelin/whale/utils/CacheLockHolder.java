package com.xxelin.whale.utils;

import java.util.WeakHashMap;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheLockHolder.java , v 0.1 2019-08-05 16:50 ElinZhou Exp $
 */
public class CacheLockHolder {

    private static final WeakHashMap<String, Object> WEAK_HASH_MAP = new WeakHashMap<>(4096);

    private CacheLockHolder() {
        //cant instance
    }

    public static Object getLock(String key) {

        Object lock = WEAK_HASH_MAP.get(key);
        if (lock != null) {
            return lock;
        }
        synchronized (WEAK_HASH_MAP) {
            lock = WEAK_HASH_MAP.get(key);
            if (lock != null) {
                return lock;
            }

            lock = new Object();
            WEAK_HASH_MAP.put(key, lock);
            return lock;
        }
    }

}
