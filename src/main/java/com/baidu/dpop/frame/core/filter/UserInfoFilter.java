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

import org.jasig.cas.client.validation.Assertion;

import com.baidu.dpop.frame.core.constant.DpopConstants;
import com.baidu.dpop.frame.core.util.CookieUtils;
import com.baidu.dpop.frame.core.util.ThreadLocalInfo;
import com.baidu.dpop.frame.core.util.UUIDUtils;
import com.baidu.dpop.frame.core.web.DpopSession;
import com.baidu.dpop.frame.core.web.DpopSessionFactory;


/**
 * 用户信息相关拦截器
 * */
public class UserInfoFilter implements Filter{
	
	/**
	 * 初始化
	 * */
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	/**
	 * 存储当前登录用户的用户信息
	 * */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;
		
		String uuid = null;
		String curUserName = null;
		
		try {
			// 获取sessionID相关cookie
			Cookie myCookie = CookieUtils.findCookieByName(httpRequest, DpopSession.USER_DPOP_SESSION_ID);
			if(myCookie != null) {
				uuid = myCookie.getValue();
			}
			
	        // 获取SSO Assertion信息中UserName
	        String uuapUserName = null;
			Assertion assertion = (Assertion)httpRequest.getSession().getAttribute(DpopConstants.CONST_CAS_ASSERTION);
			if(assertion != null) {
				uuapUserName = assertion.getPrincipal().getName();
			}
				
			// 有Cookie信息已，无Assertion
	        if(uuid != null && uuapUserName == null) {
	        	DpopSession session = DpopSessionFactory.getMySession(uuid);
	        	Object userNameObj = session.getAttribute("userName");
	        	// session信息不存在则重新添加
	        	if(userNameObj != null) {
	        		curUserName = userNameObj.toString();
	        	}
	        } 
	        // 有cookie, 有Assertion
	        else if(uuid != null && uuapUserName != null) {
	        	DpopSession session = DpopSessionFactory.getMySession(uuid);
	        	Object userName = session.getAttribute("userName");
	        	if(userName == null) {
	        		// session中新增Attribute
	        		session.setAttribute("userName", uuapUserName);
	        	}
	        	curUserName = uuapUserName;
	        }
	        // 无cookie, 有Assertion
	        else if(uuid == null && uuapUserName != null) {
	        	// new 一个UUID
	        	uuid = UUIDUtils.getUUID();
	        	
	        	// 用户信息存session
				DpopSession session = DpopSessionFactory.getMySession(uuid);
				session.setAttribute("userName", uuapUserName);
				
				// 新增对应cookie信息
                Cookie cookie = CookieUtils.createNewCookie(DpopSession.USER_DPOP_SESSION_ID, uuid, null, "/", -1);
				httpResponse.addCookie(cookie);
				
				curUserName = uuapUserName;
	        }
	        
	        // ThreadLocal中保存userName && uuid
    		ThreadLocalInfo.setAttribute(DpopConstants.DPOP_USER_NAME_ATTRIBUTE, curUserName);
    		ThreadLocalInfo.setAttribute(DpopConstants.DPOP_USER_UUID_ATTRIBUTE, uuid);
	        
			chain.doFilter(request, response);
				
		} finally {
			// 删除ThreadLocal中用户信息
			ThreadLocalInfo.removeAll();
		}
		
		
	}
	
	/**
	 * 资源清理
	 * */
	public void destroy() {
	}

}
