package com.xxelin.whale.core;

import com.alibaba.fastjson.JSON;
import com.xxelin.whale.bean.CacheClassReport;
import com.xxelin.whale.bean.CacheMethodReport;
import com.xxelin.whale.bean.CacheRecord;
import com.xxelin.whale.bean.CacheReport;
import com.xxelin.whale.bean.CacherReport;
import com.xxelin.whale.core.cacher.Cacher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: MonitorHolder.java , v 0.1 2019-08-22 11:21 ElinZhou Exp $
 */
public class MonitorHolder {

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, CacheRecord>>> DATA =
            new ConcurrentHashMap<>(128);

    private MonitorHolder() {
        //do nothing
    }

    public static void init(Class<?> clazz, String method, Cacher cacher) {
        synchronized (DATA) {
            DATA.putIfAbsent(clazz.getName(), new ConcurrentHashMap<>(128));
            ConcurrentHashMap<String, ConcurrentHashMap<String, CacheRecord>> methodMap = DATA.get(clazz.getName());
            methodMap.putIfAbsent(method, new ConcurrentHashMap<>(2));
            ConcurrentHashMap<String, CacheRecord> cacherMap = methodMap.get(method);

            cacherMap.putIfAbsent(cacher.cacheName(), new CacheRecord());
        }
    }


    public static void requestAndHit(Class<?> clazz, String method, Cacher cacher) {
        CacheRecord cacheRecord = DATA.get(clazz.getName()).get(method).get(cacher.cacheName());
        cacheRecord.getRequestTimes().increment();
        cacheRecord.getHitTimes().increment();
    }

    public static void requestAndMiss(Class<?> clazz, String method, Cacher cacher, long spend) {
        CacheRecord cacheRecord = DATA.get(clazz.getName()).get(method).get(cacher.cacheName());
        cacheRecord.getRequestTimes().increment();
        cacheRecord.getSourceBackTime().add(spend);
    }

    public static String desc() {
        CacheReport report = new CacheReport();
        List<CacheClassReport> classReportList = new ArrayList<>(DATA.size());
        long totalRequestTimes = 0;
        long totalHitTimes = 0;
        long totalSourceBackTime = 0;
        for (Map.Entry<String, ConcurrentHashMap<String, ConcurrentHashMap<String, CacheRecord>>> classEntry :
                DATA.entrySet()) {
            CacheClassReport classReport = new CacheClassReport();
            classReportList.add(classReport);
            classReport.setClassName(classEntry.getKey());

            long methodRequestTimes = 0;
            long methodHitTimes = 0;
            long methodSourceBackTime = 0;
            List<CacheMethodReport> methodReportList = new ArrayList<>(classEntry.getValue().size());
            for (Map.Entry<String, ConcurrentHashMap<String, CacheRecord>> methodEntry :
                    classEntry.getValue().entrySet()) {


                CacheMethodReport methodReport = new CacheMethodReport();
                methodReportList.add(methodReport);
                methodReport.setMethod(methodEntry.getKey());

                long requestTimes = 0;
                long hitTimes = 0;
                long sourceBackTime = 0;

                List<CacherReport> cacherReportList = new ArrayList<>(methodEntry.getValue().size());
                for (Map.Entry<String, CacheRecord> cacheEntry : methodEntry.getValue().entrySet()) {

                    CacherReport cacherReport = new CacherReport();
                    cacherReportList.add(cacherReport);

                    CacheRecord record = cacheEntry.getValue();
                    cacherReport.setCacherName(cacheEntry.getKey());
                    cacherReport.setHitTimes(record.getHitTimes().longValue());
                    cacherReport.setRequestTimes(record.getRequestTimes().longValue());
                    cacherReport.setSourceBackTime(record.getSourceBackTime().longValue());

                    requestTimes += cacherReport.getRequestTimes();
                    hitTimes += cacherReport.getHitTimes();
                    sourceBackTime += cacherReport.getSourceBackTime();
                }

                methodReport.setHitTimes(hitTimes);
                methodReport.setRequestTimes(requestTimes);
                methodReport.setSourceBackTime(sourceBackTime);
                methodReport.setCachers(cacherReportList);

                methodRequestTimes += methodReport.getRequestTimes();
                methodHitTimes += methodReport.getHitTimes();
                methodSourceBackTime += methodReport.getSourceBackTime();
            }
            classReport.setMethods(methodReportList);
            classReport.setRequestTimes(methodRequestTimes);
            classReport.setHitTimes(methodHitTimes);
            classReport.setSourceBackTime(methodSourceBackTime);

            totalRequestTimes += methodRequestTimes;
            totalHitTimes += methodHitTimes;
            totalSourceBackTime += methodSourceBackTime;
        }
        report.setClasses(classReportList);
        report.setRequestTimes(totalRequestTimes);
        report.setHitTimes(totalHitTimes);
        report.setSourceBackTime(totalSourceBackTime);
        return JSON.toJSONString(report);
    }

}
