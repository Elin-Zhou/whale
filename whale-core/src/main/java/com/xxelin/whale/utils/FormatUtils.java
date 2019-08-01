package com.xxelin.whale.utils;

import java.lang.reflect.Method;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: FormatUtils.java , v 0.1 2019-08-01 11:33 ElinZhou Exp $
 */
public class FormatUtils {

    private FormatUtils() {
        //cant instance
    }

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
}
