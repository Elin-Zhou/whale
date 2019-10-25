package com.xxelin.whale.starter.configuration;

import com.xxelin.whale.core.RedisHolder;
import com.xxelin.whale.core.synchronizer.RedisSynchronizer;
import com.xxelin.whale.message.redis.RedisPublisher;
import com.xxelin.whale.message.redis.RedisSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

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
        if (redisTemplate == null) {
            redisTemplate = context.getBean(RedisTemplate.class);
        }

        RedisHolder redisHolder = new RedisHolder(redisTemplate);
        if (redisTemplate != null) {
            RedisSynchronizer.init();
        }
        return redisHolder;
    }

    @Bean
    public RedisSubscriber redisSubscriber() {
        return new RedisSubscriber();
    }

    @Bean
    public MessageListenerAdapter listener(RedisSubscriber subscriber) {
        RedisTemplate redisTemplate = RedisHolder.getRedisTemplate();
        if (redisTemplate != null) {
            MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber);
            adapter.setSerializer(redisTemplate.getValueSerializer());
            adapter.afterPropertiesSet();
            return adapter;
        }
        return null;
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, new PatternTopic("/redis/whale"));
        return container;
    }

    @Bean
    public RedisPublisher redisPublisher() {
        return new RedisPublisher(RedisHolder.getRedisTemplate());
    }
}
