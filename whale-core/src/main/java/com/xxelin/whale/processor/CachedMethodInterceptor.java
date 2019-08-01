package com.xxelin.whale.processor;

import com.xxelin.whale.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CachedMethodInterceptor.java , v 0.1 2019-08-01 11:36 ElinZhou Exp $
 */
@Slf4j
public class CachedMethodInterceptor implements MethodInterceptor {

    private static final CopyOnWriteArraySet<Method> CACHED = new CopyOnWriteArraySet<>();

    private Object originalObject;

    public CachedMethodInterceptor(Object originalObject) {
        this.originalObject = originalObject;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        Class<?> invokeClass = originalObject.getClass();
        Class<?>[] invokeInterfaces = invokeClass.getInterfaces();

        List<Class<?>> classes = new ArrayList<>(Arrays.asList(invokeInterfaces));
        classes.add(invokeClass);

        String effectClassName = null;
        boolean cached = CACHED.contains(method);
        if (!cached) {
            for (int i = classes.size() - 1; i >= 0; i--) {
                Class<?> now = classes.get(i);
                Set<String> cachedMethods = CachedBeanProcessor.getCachedClassMethods().get(now.getName());
                if (cachedMethods != null && cachedMethods.contains(FormatUtils.format(method))) {
                    effectClassName = now.getName();
                    cached = true;
                    CACHED.add(method);
                    break;
                }
            }
        }
        if (!cached) {
            return methodProxy.invoke(originalObject, objects);
        }
        if (log.isDebugEnabled()) {
            log.debug("{}.{} use cache", effectClassName, FormatUtils.format(method));
        }
        //TODO read cache
        return methodProxy.invoke(originalObject, objects);
    }
}
