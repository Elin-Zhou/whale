package com.xxelin.whale.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Integer maxSizeLimit;

    private boolean consistency;

    private boolean cacheNull;
}
