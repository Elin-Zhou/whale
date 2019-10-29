package com.xxelin.whale.starter.configuration;

import com.xxelin.whale.core.RedisHolder;
import com.xxelin.whale.core.synchronizer.RedisSynchronizer;
import com.xxelin.whale.message.redis.RedisPublisher;
import com.xxelin.whale.message.redis.RedisSubscriber;
import com.xxelin.whale.message.redis.handler.InvalidateAllHandler;
import com.xxelin.whale.message.redis.handler.InvalidateHandler;
import com.xxelin.whale.starter.properties.WhaleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(prefix = "whale", havingValue = "true", name = "enable")
@ConditionalOnBean(RedisTemplate.class)
public class RedisConfiguration {

    @Autowired
    private WhaleProperties whaleProperties;

    @Bean
    public RedisHolder redisHolder(RedisTemplate redisTemplate) {
        RedisHolder redisHolder = new RedisHolder(redisTemplate);
        RedisSynchronizer.init();
        return redisHolder;
    }

    @Bean
    public RedisSubscriber redisSubscriber() {
        return new RedisSubscriber(whaleProperties.getRedisTopicPrefix());
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
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    public RedisPublisher redisPublisher() {
        return new RedisPublisher(RedisHolder.getRedisTemplate(), whaleProperties.getRedisTopicPrefix());
    }

    @Bean
    public InvalidateHandler invalidateHandler(RedisMessageListenerContainer container,
                                               MessageListenerAdapter listener) {

        InvalidateHandler invalidateHandler = new InvalidateHandler();
        container.addMessageListener(listener,
                new PatternTopic(whaleProperties.getRedisTopicPrefix() + invalidateHandler.topic()));
        return invalidateHandler;
    }

    @Bean
    public InvalidateAllHandler invalidateAllHandler(RedisMessageListenerContainer container,
                                                     MessageListenerAdapter listener) {

        InvalidateAllHandler invalidateAllHandler = new InvalidateAllHandler();
        container.addMessageListener(listener,
                new PatternTopic(whaleProperties.getRedisTopicPrefix() + invalidateAllHandler.topic()));
        return invalidateAllHandler;
    }
}
