package com.baidu.dpop.frame.monitor.executstack.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 程序执行的上下文，保存执行线程时序的状态
 * 
 * @author huhailiang
 * 
 */
public class ExecutContext {

    /**
     * 保持执行时序状态的容器
     */
    private static final ThreadLocal<Map<String, Object>> EXECUT_CONTEXT = new ThreadLocal<Map<String, Object>>();

    /**
     * 将当前处理线程一个上下文状态保存至时序状态容器中
     * 
     * @param key
     * @param value
     * @return
     */
    public static Object put(String key, Object value) {
        Map<String, Object> contextMap = EXECUT_CONTEXT.get();
        if (null == contextMap) {
            contextMap = new HashMap<String, Object>(15);
            EXECUT_CONTEXT.set(contextMap);
        }
        return contextMap.put(key, value);
    }

    /**
     * 获取当前处理线程一个以key的状态值，如果无则返回Null
     * 
     * @param key
     * @return
     */
    public static Object get(String key) {
        Map<String, Object> contextMap = EXECUT_CONTEXT.get();
        if (contextMap != null) {
            return contextMap.get(key);
        }
        return null;
    }

    /**
     * 清空当前线程所hold的上下文状态
     */
    public static void clear() {
        EXECUT_CONTEXT.remove();
    }

}
