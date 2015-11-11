package com.baidu.dpop.frame.monitor.executstack.context;

import com.baidu.dpop.frame.monitor.executstack.aop.ExecutStackTrace;

/**
 * 程序执行的上下文的工具类，相当于上下文的门面，
 * 将细分离出ExecutContext
 * @author huhailiang
 */
public abstract class ExecutContextUtils {

    private static final String OPEN_MONITER_SWITCH_KEY = "$_OPEN_MONITER_SWITCH_KEY";

    private static final String METHOD_STACK_KEY = "$_METHOD_STACK_KEY";

    private static final String REQUEST_ID = "$_REQUEST_ID";

    /**
     * 
     */
    public static void openExecutMoniter() {
        ExecutContext.put(OPEN_MONITER_SWITCH_KEY, Boolean.TRUE);
    }

    /**
     * 
     * @return
     */
    public static boolean isOpenExecutMoniter() {
        Boolean isOpenMoniter = (Boolean) ExecutContext.get(OPEN_MONITER_SWITCH_KEY);
        if (null != isOpenMoniter) {
            return isOpenMoniter.booleanValue();
        }
        return false;
    }

    /**
     * 
     * @return
     */
    public static ExecutStackTrace getExecutStackTrace() {
        ExecutStackTrace executStackTrace = (ExecutStackTrace) ExecutContext.get(METHOD_STACK_KEY);
        if (null == executStackTrace) {
            executStackTrace = new ExecutStackTrace();
            ExecutContext.put(METHOD_STACK_KEY, executStackTrace);
        }
        return executStackTrace;
    }

    public static String getRequestId() {
        return (String) ExecutContext.get(REQUEST_ID);
    }

    public static void setRequestId(String requestId) {
        ExecutContext.put(REQUEST_ID, requestId);
    }
}
