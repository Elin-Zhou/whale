package com.xxelin.whale.message.redis;

import com.alibaba.fastjson.JSON;
import com.xxelin.whale.message.redis.entity.SimpleMessage;
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

    private String redisTopicPrefix;

    public void publish(RedisTopic topic, SimpleMessage message) {
        redisTemplate.convertAndSend(redisTopicPrefix + topic.name(), JSON.toJSONString(message));
    }
}
