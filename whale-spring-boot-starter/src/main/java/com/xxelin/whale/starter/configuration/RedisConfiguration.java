package com.xxelin.whale.starter.configuration;

import com.xxelin.whale.core.RedisHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisConfiguration.java , v 0.1 2019-09-06 17:40 ElinZhou Exp $
 */
@Configuration
@ConditionalOnClass({RedisTemplate.class})
@ConditionalOnProperty(prefix = "whale", havingValue = "true", name = "enable")
public class RedisConfiguration {

    @Bean
    public RedisHolder redisHolder(ApplicationContext context) {
        RedisTemplate redisTemplate = context.getBean("redisTemplate", RedisTemplate.class);
        return new RedisHolder(redisTemplate);
    }

}
