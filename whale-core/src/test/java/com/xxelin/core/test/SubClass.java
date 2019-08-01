package com.xxelin.core.test;

import java.lang.reflect.Method;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: SubClass.java , v 0.1 2019-07-31 17:15 ElinZhou Exp $
 */
public class SubClass implements SuperClass {
    @Override
    public void test() {

    }


    public static void main(String... args) {
        Method[] declaredMethods = SubClass.class.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            declaredMethod.getAnnotations();
        }
    }

}
