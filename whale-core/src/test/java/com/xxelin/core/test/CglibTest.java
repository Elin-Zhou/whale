package com.xxelin.core.test;

import com.xxelin.whale.config.GlobalConfig;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CglibTest.java , v 0.1 2019-08-15 10:05 ElinZhou Exp $
 */
public class CglibTest {

    public static void main(String... args) {

        Class<?> clazz = GlobalConfig.class;

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new MethodInterceptor() {

            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                return null;
            }
        });
        Object result = enhancer.create();


        enhancer = new Enhancer();
        enhancer.setSuperclass(result.getClass());
        enhancer.setCallback(new MethodInterceptor() {

            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                return null;
            }
        });
        enhancer.create();


    }


}
