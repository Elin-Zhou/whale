package com.xxelin.whale.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: WhaleProperties.java , v 0.1 2019-07-31 14:19 ElinZhou Exp $
 */
@Data
@ConfigurationProperties(prefix = "whale")
public class WhaleProperties {

    /**
     * 是否开启缓存框架
     */
    private boolean enable;

    /**
     * 缓存的命名空间，如果不配置会自动读取Spring中的spring.application.name配置
     */
    private String namespace;

    /**
     * 缓存失效时间（秒）
     */
    private Long expireSeconds;

    /**
     * 本地缓存的最大数量，默认为Integer.MAX_VALUE
     */
    private Integer maxSizeLimit;

    /**
     * 是否开启强一致性
     */
    private boolean consistency;

    /**
     * 是否缓存null值
     */
    private boolean cacheNull;

}
