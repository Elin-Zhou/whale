package com.xxelin.whale.utils;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author ElinZhou zhoufeng@duiba.com.cn
 * @version $Id: GzipUtil.java , v 0.1 2019-01-14 16:29 ElinZhou Exp $
 */
public class GzipUtil {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GzipUtil.class);

    private GzipUtil() {
        throw new UnsupportedOperationException();
    }


    public static byte[] zip(byte[] in) {
        if (in == null) {
            throw new NullPointerException("Can't compress null");
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gz = new GZIPOutputStream(bos)) {
            gz.write(in);
            gz.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("IO exception compressing data", e);
        }
    }


    public static byte[] unzip(byte[] in) {
        ByteArrayOutputStream bos = null;
        if (in != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(in);
            bos = new ByteArrayOutputStream();
            try (GZIPInputStream gis = new GZIPInputStream(bis)) {

                byte[] buf = new byte[16 * 1024];
                int r;
                while ((r = gis.read(buf)) > 0) {
                    bos.write(buf, 0, r);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to decompress data", e);
                bos = null;
            } finally {
                IOUtils.closeQuietly(bis);
            }
        }
        return bos == null ? null : bos.toByteArray();
    }

}
