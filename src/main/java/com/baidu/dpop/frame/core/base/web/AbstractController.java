package com.baidu.dpop.frame.core.base.web;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 
 * @author huhailiang
 * @date 2014-7-5下午3:23:47
 */
public class AbstractController implements ApplicationContextAware {

    protected ApplicationContext context;
    
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context =applicationContext;
	}
	

	
	/**
	 * 从配置中读取信息
	 * @param resourceMessage
	 * @return
	 */
	public String getMessage(String resourceMessage) {
		try {
			String message = context.getMessage(resourceMessage, null,
					Locale.SIMPLIFIED_CHINESE);
			return message;
		} catch (Exception e) {
			return resourceMessage;
		}
	}
}
