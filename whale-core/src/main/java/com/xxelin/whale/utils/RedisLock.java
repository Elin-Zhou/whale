package com.xxelin.whale.utils;

public interface RedisLock extends AutoCloseable {
    void unlock();
}
