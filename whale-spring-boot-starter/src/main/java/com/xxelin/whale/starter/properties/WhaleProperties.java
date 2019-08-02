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

    private boolean enable;

    private String nameSpace;

    private boolean consistency;

    private boolean cacheNull;

}
