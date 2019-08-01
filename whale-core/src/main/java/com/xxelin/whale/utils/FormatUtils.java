package com.xxelin.whale.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: FormatUtils.java , v 0.1 2019-08-01 11:33 ElinZhou Exp $
 */
public class FormatUtils {

    private FormatUtils() {
        //cant instance
    }

    private static final Set<Class<?>> BASIC_CLASS = Sets.newHashSet(String.class, short.class, char.class, int.class,
            long.class, double.class, Boolean.class, Integer.class, Byte.class, Long.class, Double.class, Float.class
            , Character.class, Short.class, Boolean.class);

    public static String format(Method method) {
        StringBuilder sb = new StringBuilder(method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0) {
            return sb.toString();
        }
        sb.append("(").append(parameterTypes[0].getTypeName());
        if (parameterTypes.length == 1) {
            return sb.append(")").toString();
        } else {
            for (int i = 1; i < parameterTypes.length; i++) {
                sb.append(",").append(parameterTypes[i].getTypeName());
            }
            return sb.append(")").toString();
        }
    }

    public static String cacheKey(Class<?> clazz, String name, Object[] params) {
        StringBuilder sb = new StringBuilder(clazz.getName());
        sb.append(".");
        sb.append(name);
        sb.append("(");
        sb.append(Joiner.on(",").join(Arrays.stream(params).map(FormatUtils::format).collect(Collectors.toList())));
        sb.append(")");
        return sb.toString();
    }

    public static String cacheKey(Class<?> clazz, Method method, Object[] params) {
        return cacheKey(clazz, method.getName(), params);
    }


    private static String format(Object param) {
        if (param == null) {
            return "null";
        } else if (BASIC_CLASS.contains(param.getClass())) {
            return param + "/" + param.getClass().getSimpleName().toUpperCase();
        } else {
            return JSON.toJSONString(param);
        }
    }
}
