package com.xxelin.whale.message.redis.entity;

import lombok.Data;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: InvalideAllMessage.java , v 0.1 2019-10-25 18:44 ElinZhou Exp $
 */
@Data
public class InvalideAllMessage extends SimpleMessage {

    private String beanName;

    private String methodKey;

}
