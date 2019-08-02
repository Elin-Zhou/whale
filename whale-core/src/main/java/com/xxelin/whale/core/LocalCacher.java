package com.xxelin.whale.core;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: Cacher.java , v 0.1 2019-08-01 13:47 ElinZhou Exp $
 */
public interface LocalCacher extends Cacher {
    void invalidateAll();
}
