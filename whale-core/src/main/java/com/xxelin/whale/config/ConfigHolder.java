package com.xxelin.whale.config;

/**
 * @author ElinZhou eeelinzhou@gmail.com
 * @version $Id: ConfigHolder.java , v 0.1 2019-07-31 15:47 ElinZhou Exp $
 */
public class ConfigHolder {

    private static volatile boolean enable;

    private static GlobalConfig globalConfig;

    private ConfigHolder() {
        //private
    }

    public static boolean isEnable() {
        return enable;
    }

    public static void setEnable(boolean enable) {
        ConfigHolder.enable = enable;
    }

    public static GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public static void setGlobalConfig(GlobalConfig globalConfig) {
        ConfigHolder.globalConfig = globalConfig;
    }
}
