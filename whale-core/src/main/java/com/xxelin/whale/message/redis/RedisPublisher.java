package com.xxelin.whale.message.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisPublisher.java , v 0.1 2019-10-24 18:59 ElinZhou Exp $
 */
@Data
@AllArgsConstructor
public class RedisPublisher {

    private RedisTemplate redisTemplate;

    public void publish(String topic, String message) {
        redisTemplate.convertAndSend(topic, message);
    }
}
