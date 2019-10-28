package com.xxelin.whale.starter.configuration;

import com.xxelin.whale.config.GlobalConfig;
import com.xxelin.whale.processor.CachedBeanProcessor;
import com.xxelin.whale.starter.properties.WhaleProperties;
import com.xxelin.whale.utils.BeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: WhaleConfiguration.java , v 0.1 2019-07-31 14:21 ElinZhou Exp $
 */
@Configuration
@EnableConfigurationProperties(WhaleProperties.class)
@ConditionalOnProperty(prefix = "whale", havingValue = "true", name = "enable")
public class WhaleConfiguration {

    @Bean
    @ConditionalOnMissingBean(CachedBeanProcessor.class)
    public static CachedBeanProcessor cacheBeanProcessor(WhaleProperties whaleProperties, Environment environment) {
        String nameSpace = StringUtils.isNotEmpty(whaleProperties.getNamespace()) ? whaleProperties.getNamespace() :
                environment.getProperty("spring.application.name");

        if (StringUtils.isEmpty(nameSpace)) {
            throw new IllegalStateException("namespace must specified!");
        }
        GlobalConfig configuration = GlobalConfig.builder().namespace(nameSpace)
                .expireSeconds(whaleProperties.getExpireSeconds())
                .maxSizeLimit(whaleProperties.getMaxSizeLimit())
                .cacheNull(whaleProperties.isCacheNull())
                .consistency(whaleProperties.isConsistency())
                .build();
        return new CachedBeanProcessor(configuration);
    }

    @Bean
    public BeanFactory beanFactory() {
        return new BeanFactory();
    }

}
