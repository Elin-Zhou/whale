package com.xxelin.whale.processor;

import com.google.common.collect.HashMultimap;
import com.xxelin.whale.annotation.Cached;
import com.xxelin.whale.utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CachedBeanProcessor.java , v 0.1 2019-07-31 13:42 ElinZhou Exp $
 */
@Slf4j
public class CachedBeanProcessor implements BeanPostProcessor {

    private static final ConcurrentHashMap<String, Set<String>> CACHED_CLASS_METHODS = new ConcurrentHashMap<>();


    public Object postProcessAfterInitialization(Object o, String s) {
        Class<?> clazz = o.getClass();
        Class<?>[] interfaces = clazz.getInterfaces();

        HashMultimap<String, String> multimap = HashMultimap.create();

        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getAnnotation(Cached.class) != null) {
                multimap.put(clazz.getName(), FormatUtils.format(method));
                continue;
            }

            for (int i = interfaces.length - 1; i >= 0; i--) {
                try {
                    Method superMethod = interfaces[i].getDeclaredMethod(method.getName(), method.getParameterTypes());
                    if (superMethod.getAnnotation(Cached.class) != null) {
                        multimap.put(interfaces[i].getName(), FormatUtils.format(superMethod));
                        break;
                    }
                } catch (NoSuchMethodException e) {
                    //do nothing,keep search
                }
            }
        }


        boolean proxy = !multimap.isEmpty();
        //to reduce CopyOnWriteArraySet copy times
        for (Map.Entry<String, Collection<String>> entry : multimap.asMap().entrySet()) {
            CACHED_CLASS_METHODS.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        if (!proxy) {
            return o;
        }
        //if some method in this object use Cached annotation,create proxy
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new CachedMethodInterceptor(o));
        return enhancer.create();
    }

    public Object postProcessBeforeInitialization(Object o, String s) {
        return o;
    }

    static ConcurrentHashMap<String, Set<String>> getCachedClassMethods() {
        return CACHED_CLASS_METHODS;
    }
}
