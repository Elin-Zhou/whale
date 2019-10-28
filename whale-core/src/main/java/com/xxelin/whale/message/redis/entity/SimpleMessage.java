package com.xxelin.whale.message.redis.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: SimpleMessage.java , v 0.1 2019-10-15 15:27 ElinZhou Exp $
 */
@Data
public class SimpleMessage {

    private Date createTime = new Date();

}
