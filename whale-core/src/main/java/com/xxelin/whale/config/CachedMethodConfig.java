package com.xxelin.whale.config;

import com.xxelin.whale.enums.CacheType;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CachedMethodConfig.java , v 0.1 2019-08-02 11:14 ElinZhou Exp $
 */
@Data
public class CachedMethodConfig {

    private String nameSpace;

    private String name;

    private Long expire;

    private TimeUnit timeUnit;

    private Long localExpire;

    private CacheType type;

    private Integer sizeLimit;

    private boolean consistency;

    private boolean cacheNull;

}
