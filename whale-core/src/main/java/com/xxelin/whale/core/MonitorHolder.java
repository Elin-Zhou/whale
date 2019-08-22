package com.xxelin.whale.core;

import com.alibaba.fastjson.JSON;
import com.xxelin.whale.bean.CacheClassReport;
import com.xxelin.whale.bean.CacheMethodReport;
import com.xxelin.whale.bean.CacheRecord;
import com.xxelin.whale.bean.CacheReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: MonitorHolder.java , v 0.1 2019-08-22 11:21 ElinZhou Exp $
 */
public class MonitorHolder {

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, CacheRecord>> DATA =
            new ConcurrentHashMap<>(128);

    private MonitorHolder() {
        //do nothing
    }

    public static void init(Class<?> clazz, String method) {
        synchronized (DATA) {
            DATA.putIfAbsent(clazz.getName(), new ConcurrentHashMap<>(128));
            ConcurrentHashMap<String, CacheRecord> methodMap = DATA.get(clazz.getName());
            methodMap.putIfAbsent(method, new CacheRecord());
        }
    }


    public static void requestAndHit(Class<?> clazz, String method) {
        CacheRecord cacheRecord = DATA.get(clazz.getName()).get(method);
        cacheRecord.getRequestTimes().increment();
        cacheRecord.getHitTimes().increment();
    }

    public static void requestAndMiss(Class<?> clazz, String method, long spend) {
        CacheRecord cacheRecord = DATA.get(clazz.getName()).get(method);
        cacheRecord.getRequestTimes().increment();
        cacheRecord.getSourceBackTime().add(spend);
    }

    public static String desc() {
        CacheReport report = new CacheReport();
        List<CacheClassReport> classReportList = new ArrayList<>(DATA.size());
        long totalRequestTimes = 0;
        long totalHitTimes = 0;
        long totalSourceBackTime = 0;
        for (Map.Entry<String, ConcurrentHashMap<String, CacheRecord>> classEntry : DATA.entrySet()) {
            CacheClassReport classReport = new CacheClassReport();
            classReportList.add(classReport);
            classReport.setClassName(classEntry.getKey());

            long requestTimes = 0;
            long hitTimes = 0;
            long sourceBackTime = 0;
            List<CacheMethodReport> methodReportList = new ArrayList<>(classEntry.getValue().size());
            for (Map.Entry<String, CacheRecord> methodEntry : classEntry.getValue().entrySet()) {
                CacheMethodReport methodReport = new CacheMethodReport();
                methodReportList.add(methodReport);
                CacheRecord record = methodEntry.getValue();
                methodReport.setMethod(methodEntry.getKey());
                methodReport.setHitTimes(record.getHitTimes().longValue());
                methodReport.setRequestTimes(record.getRequestTimes().longValue());
                methodReport.setSourceBackTime(record.getSourceBackTime().longValue());

                requestTimes += methodReport.getRequestTimes();
                hitTimes += methodReport.getHitTimes();
                sourceBackTime += methodReport.getSourceBackTime();
            }
            classReport.setMethods(methodReportList);
            classReport.setRequestTimes(requestTimes);
            classReport.setHitTimes(hitTimes);
            classReport.setSourceBackTime(sourceBackTime);

            totalRequestTimes += requestTimes;
            totalHitTimes += hitTimes;
            totalSourceBackTime += sourceBackTime;
        }
        report.setClasses(classReportList);
        report.setRequestTimes(totalRequestTimes);
        report.setHitTimes(totalHitTimes);
        report.setSourceBackTime(totalSourceBackTime);
        return JSON.toJSONString(report);
    }

}
