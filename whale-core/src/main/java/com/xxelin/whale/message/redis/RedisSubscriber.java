package com.xxelin.whale.message.redis;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisSubscriber.java , v 0.1 2019-10-15 15:23 ElinZhou Exp $
 */
@Slf4j
public class RedisSubscriber {

    public void handleMessage(String message, String pattern) {
        log.info("收到消息", message);
    }
}
