package com.xxelin.whale.bean;

import lombok.Data;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: CacherReport.java , v 0.1 2019-09-09 15:50 ElinZhou Exp $
 */
@Data
public class CacherReport {
    private long requestTimes = 0;
    private long hitTimes = 0;
    private long sourceBackTime = 0;

    private String cacherName;

}
