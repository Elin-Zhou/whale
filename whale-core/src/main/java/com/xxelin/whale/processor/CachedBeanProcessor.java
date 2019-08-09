package com.xxelin.whale.processor;

import com.xxelin.whale.annotation.Cached;
import com.xxelin.whale.config.GlobalConfig;
import com.xxelin.whale.core.CacheAdvance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CachedBeanProcessor.java , v 0.1 2019-07-31 13:42 ElinZhou Exp $
 */
@Slf4j
public class CachedBeanProcessor implements BeanPostProcessor {

    private GlobalConfig globalConfig;

    public CachedBeanProcessor(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    @SuppressWarnings("unchecked")
    public Object postProcessAfterInitialization(Object o, String s) {
        Class<?> clazz = o.getClass();

        Method[] methods = clazz.getDeclaredMethods();
        Map<Method, Cached> cachedMap = new HashMap<>(methods.length);
        for (Method method : methods) {
            Cached cached = AnnotationUtils.findAnnotation(method, Cached.class);
            if (cached != null) {
                cachedMap.put(method, cached);
            }
        }

        if (cachedMap.isEmpty()) {
            return o;
        }
        //if some method in this object use Cached annotation,create bean proxy
        Object proxy;

        if (!Modifier.isFinal(clazz.getModifiers())) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(clazz);
            enhancer.setInterfaces(new Class[]{CacheAdvance.class});
            enhancer.setCallback(new CachedMethodInterceptor(o, cachedMap, globalConfig));
            proxy = enhancer.create();
        } else {
            //if target class is final,use jdk dynamic proxy
            Class<?>[] original = clazz.getInterfaces();
            Class<?>[] interfaces = Arrays.copyOf(original, original.length + 1);
            interfaces[original.length] = CacheAdvance.class;
            proxy = Proxy.newProxyInstance(o.getClass().getClassLoader(), interfaces, new CachedMethodInterceptor(o,
                    cachedMap, globalConfig));
        }
        if (proxy instanceof SelfAware) {
            ((SelfAware) proxy).setSelf(proxy);
        }
        return proxy;

    }

    public Object postProcessBeforeInitialization(Object o, String s) {
        return o;
    }
}
