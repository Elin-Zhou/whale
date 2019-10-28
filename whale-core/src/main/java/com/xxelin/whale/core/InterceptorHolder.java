package com.xxelin.whale.core;

import com.xxelin.whale.processor.CachedMethodInterceptor;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: InterceptorHolder.java , v 0.1 2019-10-25 19:34 ElinZhou Exp $
 */
public class InterceptorHolder {

    private InterceptorHolder() {
        throw new UnsupportedOperationException();
    }

    private static final ConcurrentHashMap<String, CachedMethodInterceptor> INTERCEPTOR_MAP =
            new ConcurrentHashMap<>(128);

    public static void register(String beanName, CachedMethodInterceptor interceptor) {
        INTERCEPTOR_MAP.put(beanName, interceptor);
    }

    public static Optional<CachedMethodInterceptor> getInterceptor(String beanName) {
        return Optional.ofNullable(INTERCEPTOR_MAP.get(beanName));
    }


}
