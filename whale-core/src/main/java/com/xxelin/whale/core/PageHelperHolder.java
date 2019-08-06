package com.xxelin.whale.core;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: PageHelperHolder.java , v 0.1 2019-08-06 14:22 ElinZhou Exp $
 */
@Slf4j
public class PageHelperHolder {

    public static final String CLASS_NAME = "com.github.pagehelper.PageHelper";

    private static boolean usePageHelper = false;

    private static Method clearPage = null;

    static {
        try {
            Class<?> clazz = Class.forName(CLASS_NAME);
            clearPage = clazz.getMethod("clearPage");
            usePageHelper = true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            //not user PageHelper
        }

    }


    public PageHelperHolder() {
        usePageHelper = false;
    }

    public static void clear() {
        if (usePageHelper && clearPage != null) {
            try {
                clearPage.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("invoke pagehelper.clearPage error", e);
            }
        }
    }
}
