package com.xxelin.whale.starter.properties;

import com.xxelin.whale.enums.CacheType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: WhaleProperties.java , v 0.1 2019-07-31 14:19 ElinZhou Exp $
 */
@Data
@ConfigurationProperties(prefix = "whale")
public class WhaleProperties {

    private boolean enable;

    private String nameSpace;

    private Long expire;

    private TimeUnit timeUnit;

    private Long localExpire;

    private CacheType type;

    private Integer sizeLimit;

    private boolean consistency;

    private boolean cacheNull;

}
