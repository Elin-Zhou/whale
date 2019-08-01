package com.xxelin.core.test;

import com.xxelin.whale.config.GlobalConfig;
import org.junit.Test;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: GlobalTest.java , v 0.1 2019-07-31 11:02 ElinZhou Exp $
 */
public class GlobalTest {

    @Test
    public void test() {
        GlobalConfig globalConfig = GlobalConfig.builder().build();
        new GlobalConfig();
        globalConfig.setCacheNull(false);
    }

}
