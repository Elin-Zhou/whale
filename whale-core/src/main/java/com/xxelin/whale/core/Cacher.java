package com.xxelin.whale.core;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: Cacher.java , v 0.1 2019-08-01 13:47 ElinZhou Exp $
 */
public interface Cacher {

    String cacheName();

    <T> T load(String cacheKey, SourceBack<T> method) throws Exception;

    void invalidate(String key);
}
