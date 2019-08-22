package com.xxelin.whale.bean;

import lombok.Data;

import java.util.List;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheReport.java , v 0.1 2019-08-22 13:48 ElinZhou Exp $
 */
@Data
public class CacheReport {
    private long requestTimes = 0;
    private long hitTimes = 0;
    private long sourceBackTime = 0;

    private List<CacheClassReport> classes;
}
