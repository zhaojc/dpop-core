package com.baidu.dpop.frame.core.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.client.session.SingleSignOutFilter;

import com.baidu.dpop.frame.core.util.CookieUtils;
import com.baidu.dpop.frame.core.web.DpopSession;
import com.baidu.dpop.frame.core.web.DpopSessionFactory;

/**   
 * uuap退出filter封装
 * @author cgd  
 * @date 2014年9月2日 上午11:14:45 
 */
public class DpopSingleSignOutFilter implements Filter {
	
	// uuap原始退出filter
	private SingleSignOutFilter singleSignOutFilter;

	/** 
	* 初始化 
	* @param filterConfig
	* @throws ServletException  参数
	*/
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		singleSignOutFilter = new SingleSignOutFilter();
		DpopFilterConfig dpopFilterConfig = new DpopFilterConfig(filterConfig);
		singleSignOutFilter.init(dpopFilterConfig);
	}

	/** 
	*  UUAP用户登录退出Filter
	*  
	* @param request
	* @param response
	* @param chain
	* @throws IOException
	* @throws ServletException  参数
	*/
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		
		// 获取sessionID相关cookie
        String uuid = null;
        Cookie myCookie = CookieUtils.findCookieByName(httpRequest, DpopSession.USER_DPOP_SESSION_ID);
        if(myCookie != null) {
        	uuid = myCookie.getValue();
        }
        
        // 退出前清除session && cookie
        if(uuid != null) {
        	DpopSession session = DpopSessionFactory.getMySession(uuid);
        	if(session != null) {
        		session.removeAll();
        	}
        }
        if(myCookie != null) {
        	myCookie.setValue(null);
        	myCookie.setMaxAge(0);
        	httpResponse.addCookie(myCookie);
        }
        
		// 走UUAP的filter处理
		singleSignOutFilter.doFilter(request, response, chain);
	}

	/** 
	* 资源清理
	*/
	@Override
	public void destroy() {
		singleSignOutFilter.destroy();
	}

}
