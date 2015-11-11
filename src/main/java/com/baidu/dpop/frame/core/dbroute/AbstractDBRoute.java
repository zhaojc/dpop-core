package com.baidu.dpop.frame.core.dbroute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @ClassName: AbstractRoute
 * @Description: 分库分表的抽象类，把表路由公共信息做了一个汇总
 * @author huhailiang
 * @date 2014-10-23 12:22:59
 * 
 */
public abstract class AbstractDBRoute implements DBRoute {

    /**
     * 原始的RouteConfig
     */
    protected String routeConfig;

    /**
     * 库路由规则映射表
     */
    protected Map<String, String> routeRuleMap = new HashMap<String, String>();

    /**
     * 表路由字段名
     */
    protected String tableRouteFieldName = "";

    /**
     * 表名前缀
     */
    protected String tableNamePrefix = "";

    /**
     * 表名后缀生产规则
     */
    protected String tableNamePostfixFormat;

    /**
     * 其他未知属性
     */
    protected Map<String, String> attribute = new HashMap<String, String>();

    /**
     * 解析分库配置字符串
     * 
     * @Title: parseRouteConfig
     * @Description: TODO
     * @param @param routeConfig
     * @param @throws Exception
     * @return void
     * @throws
     */
    public abstract void parseRouteConfig(String routeConfig) throws Exception;

    public String getRouteConfig() {
        return routeConfig;
    }

    public void setRouteConfig(String routeConfig) {
        this.routeConfig = routeConfig;
    }

    public Map<String, String> getRouteRuleMap() {
        // 这个路由映射关系 只有在解析配置的时候可以被修改，其他都只是只读
        return Collections.unmodifiableMap(routeRuleMap);
    }

    public void setRouteRuleMap(Map<String, String> routeRuleMap) {
        this.routeRuleMap = routeRuleMap;
    }

    public String getTableNamePrefix() {
        return tableNamePrefix;
    }

    public void setTableNamePrefix(String tableNamePrefix) {
        this.tableNamePrefix = tableNamePrefix;
    }

    public String getAttribute(String key) {
        return attribute.get(key);
    }

    public void setAttribute(String key, String value) {
        this.attribute.put(key, value);
    }

    public String getTableRouteFieldName() {
        return tableRouteFieldName;
    }

    public void setTableRouteFieldName(String tableRouteFieldName) {
        this.tableRouteFieldName = tableRouteFieldName;
    }

    public String getTableNamePostfixFormat() {
        return tableNamePostfixFormat;
    }

    public void setTableNamePostfixFormat(String tableNamePostfixFormat) {
        this.tableNamePostfixFormat = tableNamePostfixFormat;
    }

}
