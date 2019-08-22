package com.xxelin.whale.bean;

import lombok.Data;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacheRecord.java , v 0.1 2019-08-22 11:40 ElinZhou Exp $
 */
@Data
public class CacheRecord {

    private LongAdder requestTimes = new LongAdder();

    private LongAdder hitTimes = new LongAdder();

    private LongAdder sourceBackTime = new LongAdder();
}
