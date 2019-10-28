package com.xxelin.whale.message.redis.handler;

import com.xxelin.whale.message.redis.RedisSubscriber;
import com.xxelin.whale.message.redis.RedisTopic;
import com.xxelin.whale.message.redis.entity.SimpleMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: RedisMessageHandler.java , v 0.1 2019-10-25 18:17 ElinZhou Exp $
 */
@Slf4j
public abstract class RedisMessageHandler<T extends SimpleMessage> implements InitializingBean {

    public abstract void handler(T message);

    public abstract RedisTopic topic();

    private void register(RedisTopic topic) {
        RedisSubscriber.HANDLER_MAP.put(topic.name(), this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        register(topic());
    }

    /**
     * 得到子类中泛型参数的具体类型
     *
     * @return
     */
    public Type getType() {
        Type genericSuperclass = this.getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
            return parameterizedType.getActualTypeArguments()[0];
        }
        return null;
    }
}
