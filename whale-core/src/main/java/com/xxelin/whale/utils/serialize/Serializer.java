package com.xxelin.whale.utils.serialize;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: Serializer.java , v 0.1 2019-09-08 16:04 ElinZhou Exp $
 */
public interface Serializer {

    <T> byte[] serialize(T object);

    <T> T deserialize(byte[] bytes);
}
