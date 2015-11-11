package com.baidu.dpop.frame.core.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.baidu.dpop.frame.core.beans.ServiceLocator;

/**   
 * 容器初始化相关Listener
 * @author cgd  
 * @date 2014年9月1日 下午4:03:20 
 */
public class InitSystemListener implements ServletContextListener {

	/** 
	* 容器初始化 
	* @param sce  参数
	*/
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		WebApplicationContext context = WebApplicationContextUtils.
				getWebApplicationContext(sce.getServletContext());
		
		// factory set
		ServiceLocator.setFactory(context);
		
	}

	/** 
	* 资源清理
	* @param sce  参数
	*/
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// factory清除
		ServiceLocator.setFactory(null);
	}

}
