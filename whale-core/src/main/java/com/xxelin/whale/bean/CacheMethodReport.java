package com.xxelin.whale.bean;

import lombok.Data;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheMethodReport.java , v 0.1 2019-08-22 13:50 ElinZhou Exp $
 */
@Data
public class CacheMethodReport {

    private long requestTimes = 0;
    private long hitTimes = 0;
    private long sourceBackTime = 0;
    private String method;

}
