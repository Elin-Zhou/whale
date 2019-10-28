package com.xxelin.whale.message.redis.entity;

import lombok.Data;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: InvalideMessage.java , v 0.1 2019-10-25 18:43 ElinZhou Exp $
 */
@Data
public class InvalideMessage extends SimpleMessage {

    private String beanName;

    private String methodKey;

    private String cacheKey;

}
