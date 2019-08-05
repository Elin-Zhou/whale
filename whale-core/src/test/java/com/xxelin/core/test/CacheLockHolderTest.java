package com.xxelin.core.test;

import com.xxelin.whale.utils.CacheLockHolder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheLockHolderTest.java , v 0.1 2019-08-05 16:53 ElinZhou Exp $
 */
public class CacheLockHolderTest {

    @Test
    public void test_gc() {

//        String a = "abc";
////        Object lock = CacheLockHolder.getLock(a);
////        System.out.println(lock == CacheLockHolder.getLock("abc"));
////        a = null;
////
////        System.gc();
////        System.gc();
////
////        System.out.println(lock == CacheLockHolder.getLock("abc"));


        Map<Integer, Object> map = new HashMap<>();

        int count = 5000;

        for (int i = 0; i < count; i++) {
            map.put(i, CacheLockHolder.getLock("abc" + i));
        }
        for (int i = 0; i < count; i++) {
            System.out.println(CacheLockHolder.getLock("abc" + i) == map.get(i));
        }


    }

}
