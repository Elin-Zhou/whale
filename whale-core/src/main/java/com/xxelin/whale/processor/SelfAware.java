package com.xxelin.whale.processor;

import com.xxelin.whale.annotation.Cached;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: SelfAware.java , v 0.1 2019-08-09 15:44 ElinZhou Exp $
 */
public interface SelfAware<T> {
    /**
     * 在spring启动时，会把实现了此接口的对象从此方法中传入
     *
     * @param bean
     */
    @Cached(enable = false)
    void setSelf(T bean);
}
