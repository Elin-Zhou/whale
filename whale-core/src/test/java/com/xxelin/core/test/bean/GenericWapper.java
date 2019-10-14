package com.xxelin.core.test.bean;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: GenericWapper.java , v 0.1 2019-10-14 14:46 ElinZhou Exp $
 */
public class GenericWapper<T> {

    private T t;

    private int a;

    public GenericWapper() {
    }

    public GenericWapper(T t, int a) {
        this.t = t;
        this.a = a;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }
}
