package com.xxelin.whale.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Optional;

public class BeanFactory implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        BeanFactory.applicationContext = applicationContext;
    }

    public static Optional<Object> getBean(String beanName) {
        return Optional.ofNullable(applicationContext.getBean(beanName));
    }

    public static <T> Optional<T> getBean(Class<T> beanType) {
        return Optional.ofNullable(applicationContext.getBean(beanType));
    }

}
