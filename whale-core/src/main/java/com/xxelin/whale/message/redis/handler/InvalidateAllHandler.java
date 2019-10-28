package com.xxelin.whale.message.redis.handler;

import com.xxelin.whale.core.InterceptorHolder;
import com.xxelin.whale.core.cacher.LocalCacher;
import com.xxelin.whale.message.redis.RedisTopic;
import com.xxelin.whale.message.redis.entity.InvalideAllMessage;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: InvalidateAllHandler.java , v 0.1 2019-10-28 09:47 ElinZhou Exp $
 */
public class InvalidateAllHandler extends RedisMessageHandler<InvalideAllMessage> {

    @Override
    public void handler(InvalideAllMessage message) {
        InterceptorHolder.getInterceptor(message.getBeanName())
                .ifPresent(i -> i.getLocalCacher(message.getMethodKey()).ifPresent(LocalCacher::invalidateAll));
    }

    @Override
    public RedisTopic topic() {
        return RedisTopic.INVALIDATE_ALL;
    }


}
