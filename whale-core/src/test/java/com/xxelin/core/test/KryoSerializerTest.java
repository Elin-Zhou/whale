package com.xxelin.core.test;

import com.xxelin.core.test.bean.GenericWapper;
import com.xxelin.whale.utils.serialize.KryoSerializer;
import com.xxelin.whale.utils.serialize.Serializer;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: KryoSerializerTest.java , v 0.1 2019-09-08 16:25 ElinZhou Exp $
 */
public class KryoSerializerTest {

    private Serializer serializer = new KryoSerializer();

    @Test
    public void test() {
        Entity entity = new Entity();
        entity.setWords("hello world");
        entity.setTime(System.currentTimeMillis());
        byte[] bytes = new byte[40960];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) RandomUtils.nextInt(0, 128);
        }
        entity.setData(bytes);

        byte[] data = serializer.serialize(entity);

        Entity deserialize = serializer.deserialize(data, this.getClass().getClassLoader());

        Assert.assertEquals(entity.getWords(), deserialize.getWords());
        Assert.assertEquals(entity.getTime(), entity.getTime());
        Assert.assertArrayEquals(entity.getData(), deserialize.getData());


    }

    @Test
    public void generic() {

        Entity entity = new Entity();
        entity.setWords("hello world");
        entity.setTime(System.currentTimeMillis());
        byte[] bytes = new byte[40960];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) RandomUtils.nextInt(0, 128);
        }
        entity.setData(bytes);

        GenericWapper<Entity> g = new GenericWapper<>(entity, 10);

        byte[] data = serializer.serialize(g);

        GenericWapper<Entity> deserialize = serializer.deserialize(data, this.getClass().getClassLoader());

        Assert.assertEquals(entity.getWords(), deserialize.getT().getWords());
        Assert.assertEquals(entity.getTime(), deserialize.getT().getTime());
        Assert.assertArrayEquals(entity.getData(), deserialize.getT().getData());


    }


}


