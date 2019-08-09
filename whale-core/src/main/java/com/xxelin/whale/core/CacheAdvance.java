package com.xxelin.whale.core;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheAdvance.java , v 0.1 2019-08-09 13:43 ElinZhou Exp $
 */
public interface CacheAdvance {

    void invalidateAll$Proxy(String methodKey);

    void invalidate$Proxy(String methodKey, Object... params);

    void invalidateWithId$Proxy(String methodKey, String id);

}
