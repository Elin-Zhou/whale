package com.xxelin.whale.starter.parser;

import com.xxelin.whale.config.ConfigHolder;
import com.xxelin.whale.config.GlobalConfig;
import com.xxelin.whale.starter.properties.WhaleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: ConfigParser.java , v 0.1 2019-07-31 15:37 ElinZhou Exp $
 */
public class ConfigParser {

    @Autowired
    private WhaleProperties whaleProperties;

    private String nameSpace;

    public ConfigParser(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    @EventListener(ContextRefreshedEvent.class) // 应用启动之后再开启任务。
    public void onContextRefreshed(ContextRefreshedEvent event) {

        GlobalConfig configuration = GlobalConfig.builder().nameSpace(nameSpace)
                .expire(whaleProperties.getExpire())
                .timeUnit(whaleProperties.getTimeUnit())
                .localExpire(whaleProperties.getLocalExpire())
                .sizeLimit(whaleProperties.getSizeLimit())
                .cacheNull(whaleProperties.isCacheNull())
                .type(whaleProperties.getType())
                .consistency(whaleProperties.isConsistency())
                .build();

        ConfigHolder.setGlobalConfig(configuration);
        ConfigHolder.setEnable(true);

    }
}
