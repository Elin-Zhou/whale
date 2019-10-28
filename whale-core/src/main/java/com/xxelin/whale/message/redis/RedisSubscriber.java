package com.xxelin.whale.message.redis;

import com.alibaba.fastjson.JSON;
import com.xxelin.whale.message.redis.handler.RedisMessageHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisSubscriber.java , v 0.1 2019-10-15 15:23 ElinZhou Exp $
 */
@Slf4j
@AllArgsConstructor
public class RedisSubscriber {

    public static final Map<String, RedisMessageHandler<?>> HANDLER_MAP = new HashMap<>();

    private String redisTopicPrefix;

    public void handleMessage(String message, String topic) {
        RedisMessageHandler<?> redisMessageHandler = HANDLER_MAP.get(topic.replace(redisTopicPrefix, ""));
        if (redisMessageHandler == null) {
            log.warn("topic:{} can not find handler", topic);
            return;
        }
        redisMessageHandler.handler(JSON.parseObject(message, redisMessageHandler.getType()));
    }
}
