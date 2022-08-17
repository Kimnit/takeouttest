package com.kimnit.reggie.common;

/**
 * 基于threadLocal封装工具类，用于保存和获取当前用户ID
 */
public class BaseConext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<> ();

    public static void setThreadLocalId(Long id){
        threadLocal.set (id);
    }

    public static Long getThreadLocalId(){
        return threadLocal.get ();
    }
}
