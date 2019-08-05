package com.xxelin.whale.core;

import com.xxelin.whale.config.CachedMethodConfig;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: Cacher.java , v 0.1 2019-08-01 13:47 ElinZhou Exp $
 */
public interface Cacher {

    <T> T load(String key, SourceBack<T> method, CachedMethodConfig config) throws Exception;

    void invalidate(String key);
}
