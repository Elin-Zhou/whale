package com.xxelin.whale.core;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: SourceBack.java , v 0.1 2019-08-02 15:07 ElinZhou Exp $
 */
@FunctionalInterface
public interface SourceBack<T> {

    T get() throws Exception;

}
