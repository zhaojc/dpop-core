package com.baidu.dpop.frame.core.filter;

import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.baidu.dpop.frame.core.context.DpopPropertyUtils;

/**
 * 
 * @author huhailiang
 *
 */
public class DpopFilterConfig implements FilterConfig {

    private static final Logger logger = Logger.getLogger(DpopFilterConfig.class);
    
	private FilterConfig filterConfig;
	
	
	public DpopFilterConfig(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}
	

	@Override
	public String getFilterName() {
		return filterConfig.getFilterName();
	}

	@Override
	public ServletContext getServletContext() {
		return filterConfig.getServletContext();
	}

	@Override
	public String getInitParameter(String name) {
		String value = filterConfig.getInitParameter(name);
		String valueInSpringPropertys =  DpopPropertyUtils.replacePlaceholders(value, value);
		if(null != value){
			logger.info(String.format("DpopFilterConfig.getInitParameter(%s) is : %s", 
					value,valueInSpringPropertys));
		}
		return valueInSpringPropertys;
	}

	@Override
	public Enumeration getInitParameterNames() {
		return filterConfig.getInitParameterNames();
	}

}
