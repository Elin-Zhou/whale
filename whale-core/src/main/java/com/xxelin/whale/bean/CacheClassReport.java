package com.xxelin.whale.bean;

import lombok.Data;

import java.util.List;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheClassReport.java , v 0.1 2019-08-22 13:49 ElinZhou Exp $
 */
@Data
public class CacheClassReport {

    private long requestTimes = 0;
    private long hitTimes = 0;
    private long sourceBackTime = 0;

    private String className;

    private List<CacheMethodReport> methods;
}
