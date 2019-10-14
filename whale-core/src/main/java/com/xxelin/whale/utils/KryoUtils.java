package com.xxelin.whale.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: KryoUtils.java , v 0.1 2019-09-08 15:54 ElinZhou Exp $
 */
public class KryoUtils {

    private KryoUtils() {
        //do nothing
    }

    public static <T> byte[] serialize(T object) {
        Kryo kryo = new Kryo();
        Output output = new Output(256, 1024 * 1024 * 10);
        kryo.writeClassAndObject(output, object);
        output.flush();
        output.close();
        return output.toBytes();
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes, ClassLoader classLoader) {
        Kryo kryo = new Kryo();
        kryo.setClassLoader(classLoader);
        return (T) kryo.readClassAndObject(new Input(bytes));
    }

}
