package com.xxelin.whale.message.redis.handler;

import com.xxelin.whale.core.InterceptorHolder;
import com.xxelin.whale.message.redis.RedisTopic;
import com.xxelin.whale.message.redis.entity.InvalideMessage;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: InvalidateHandler.java , v 0.1 2019-10-25 19:05 ElinZhou Exp $
 */
public class InvalidateHandler extends RedisMessageHandler<InvalideMessage> {

    @Override
    public void handler(InvalideMessage message) {
        InterceptorHolder.getInterceptor(message.getBeanName()).ifPresent(i -> i.getCacher(message.getMethodKey())
                .forEach(c -> c.invalidate(message.getCacheKey())));
    }

    @Override
    public RedisTopic topic() {
        return RedisTopic.INVALIDATE;
    }
}
