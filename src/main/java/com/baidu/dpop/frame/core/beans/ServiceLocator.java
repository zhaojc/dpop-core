package com.baidu.dpop.frame.core.beans;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**   
 * 在Web Context容器中Bean定位
 * @author cgd  
 * @date 2014年9月1日 下午4:05:44 
 */
public class ServiceLocator {
	
	private static Logger LOG = Logger.getLogger(ServiceLocator.class);
	
	// Web容器
	private static ApplicationContext factory = null;
	// 单例
	private static ServiceLocator serviceLocator;
	
	
	/**
	 * 获取Bean定位器Instance（单例模式）
	 * */
	public static ServiceLocator getInstance() {
		if(serviceLocator == null) {
			serviceLocator = new ServiceLocator();
		}
		
		return serviceLocator;
	}
	
	/**
	 * 获取web容器context
	 * */
	public static ApplicationContext getFactory() {
        if (factory == null) {
        	LOG.error("ServiceLocator.factory is null maybe not config InitSystemListener correctly in web.xml");
        }
        return factory;
	}
	
	/**
	 * 在容器中获取指定BeanName的Bean
	 * */
	public static Object getBean(String beanName) {
		return getFactory().getBean(beanName);
	}
	
	/**
	 * 通过Bean类型在容器中查找Beans
	 * 
	 * @param type Bean类型
	 * */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, Object> getBeansOfType(Class type) {
		return getFactory().getBeansOfType(type);
	}
	
	public static void setFactory(ApplicationContext factory) {
		ServiceLocator.factory = factory;
	}
	
	
}
