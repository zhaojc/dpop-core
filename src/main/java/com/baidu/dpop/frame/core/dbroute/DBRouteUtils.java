package com.baidu.dpop.frame.core.dbroute;

import java.util.HashMap;
import java.util.Map;

import com.baidu.dpop.frame.core.constant.SymbolConstant;
/**
 * 
 * @ClassName: DBRouteUtils 
 * @Description: 获取分库分表的信息的工具类
 * @author huhailiang 
 * @date 2014-10-23 下午7:07:30 
 *
 */
public class DBRouteUtils {

    //只读
    private Map<String,String> routeRuleConfigMap = new HashMap<String, String>();
    
    //只读
    private Map<String,Class> routeClassMap = new HashMap<String, Class>();

    private Map<String, String> dbRoutePropertiesMap = new HashMap<String, String>();
    
    public void init() {
        if(null == dbRoutePropertiesMap || dbRoutePropertiesMap.isEmpty()){
            return;
        }
        for(Map.Entry<String, String> entry : dbRoutePropertiesMap.entrySet()){
            initRouteItem(entry.getKey(),entry.getValue());
        }
        
    }
    
    private void initRouteItem(String key,String ruleValue){
        
        String[] ruleValueArr = ruleValue.split("\\"+SymbolConstant.SYMBOL_OPEN_BRACKET);
        
        String routeClass = ruleValueArr[0];
        
        String routeConfig = ruleValueArr[1].substring(0, ruleValueArr[1].length()-1);
        
        Class routeClazzBean = null;
        Object routeBean = null;
        try {
            routeClazzBean = Class.forName(routeClass);
            routeBean = routeClazzBean.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        if(routeBean instanceof DBRoute){
            routeClassMap.put(key, routeClazzBean);
            routeRuleConfigMap.put(key, routeConfig);
        }else{
            throw new RuntimeException("routeClazz["+routeClass+"]  muste be implements interface of IDBRoute ");
        }
    }
    
    /**
     * 
     * @param clazz
     * @return
     */
    @SuppressWarnings("rawtypes")
    public <E extends DBRoute> E getRoute(Class clazz){
        String clazzName = clazz.getName();
        DBRoute route = getDBRoute(clazzName);
        return (E) route;
    }
    
    
    /**
     * 
     * @param key
     * @return
     */
    public <E extends DBRoute> E getRoute(String key){
        DBRoute route = getDBRoute(key);
        return (E) route;
    }
    
    private DBRoute getDBRoute(String key){
        
        Class routeClazzBean = routeClassMap.get(key);
        if (null == routeClazzBean) {
            return null;
        }
        DBRoute route = null;
        try {
            route = (DBRoute) routeClazzBean.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("getRoute(" + key + ") has error ", e);
        }

        String routeConfig = routeRuleConfigMap.get(key);

        if (route instanceof AbstractDBRoute) {
            try {
                ((AbstractDBRoute) route).parseRouteConfig(routeConfig);
            } catch (Exception e) {
                throw new RuntimeException("routeConfig[" + routeConfig + "] has error ", e);
            }
        }

        return route;
    }
    
    public String getRouteRuleConfig(String key){
        return routeRuleConfigMap.get(key);
    }

    public void setDbRoutePropertiesMap(Map<String, String> dbRoutePropertiesMap) {
        this.dbRoutePropertiesMap = dbRoutePropertiesMap;
    }
}
