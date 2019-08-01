package com.xxelin.whale.config;

import com.xxelin.whale.enums.CacheType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: GlobalConfig.java , v 0.1 2019-07-31 10:56 ElinZhou Exp $
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalConfig {

    private String nameSpace;

    private Long expire;

    private TimeUnit timeUnit;

    private Long localExpire;

    private CacheType type;

    private Integer sizeLimit;

    private boolean consistency;

    private boolean cacheNull;
}
