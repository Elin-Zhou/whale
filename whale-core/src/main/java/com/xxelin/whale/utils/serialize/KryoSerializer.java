package com.xxelin.whale.utils.serialize;

import com.xxelin.whale.utils.GzipUtil;
import com.xxelin.whale.utils.KryoUtils;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: KryoSerializer.java , v 0.1 2019-09-08 16:05 ElinZhou Exp $
 */
public class KryoSerializer implements Serializer {

    private static final int GZIP_THRESHOLD = 2048;

    private static final byte[] MAGIC_NUMBER = new byte[]{87, 72, 65, 76, 69};

    private static final byte[] EMPTY_ARRAY = new byte[0];

    @Override
    public <T> byte[] serialize(T object) {

        if (object == null) {
            return EMPTY_ARRAY;
        }

        byte[] bytes = KryoUtils.serialize(object);
        byte flag = 0;
        if (bytes.length > GZIP_THRESHOLD) {
            flag = 1;
            bytes = GzipUtil.zip(bytes);
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(MAGIC_NUMBER);
            out.write(flag);
            out.write(bytes);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        if (bytes.length <= MAGIC_NUMBER.length + 1 || !Arrays.equals(MAGIC_NUMBER, Arrays.copyOfRange(bytes, 0,
                MAGIC_NUMBER.length))) {
            return null;
        }
        byte[] data = Arrays.copyOfRange(bytes, MAGIC_NUMBER.length + 1, bytes.length);
        byte flag = bytes[MAGIC_NUMBER.length];

        if (flag == 1) {
            data = GzipUtil.unzip(data);
        }
        return KryoUtils.deserialize(data);
    }
}
