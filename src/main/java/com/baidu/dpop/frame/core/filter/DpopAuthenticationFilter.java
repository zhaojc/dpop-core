package com.baidu.dpop.frame.core.filter;

import java.io.IOException;
import java.util.TreeSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.springframework.util.CollectionUtils;

import com.baidu.dpop.frame.core.util.CookieUtils;
import com.baidu.dpop.frame.core.util.UrlUtils;
import com.baidu.dpop.frame.core.web.DpopSession;
import com.baidu.dpop.frame.core.web.DpopSessionFactory;

/**
 * 
 * 重写百度认证平台SSO拦截器
 * 
 */
public class DpopAuthenticationFilter implements Filter {
	
	private AuthenticationFilter authenticationFilter;
	
	/**
	 * 不做拦截的路径
	 * */
	private TreeSet<String> excludePathSet;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		DpopFilterConfig dpopFilterConfig = new DpopFilterConfig(filterConfig);
		// 初始化SSO Auth拦截器
		authenticationFilter = new AuthenticationFilter();
		authenticationFilter.init(dpopFilterConfig);
		
		// 获取配置的excludePath
		String excludePaths = dpopFilterConfig.getInitParameter("excludePaths");
		excludePathSet = this.getExcludePathSet(excludePaths);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		
		// -------------- 是否有配置excludePaths  ------------------
        if(!CollectionUtils.isEmpty(excludePathSet)) {
        	// 属于exclude path不要验证，直接跳过
            if (UrlUtils.urlMatch(excludePathSet, ((HttpServletRequest) request).getServletPath())) {
            	chain.doFilter(request, response);
                return;
            } 
        }
        
        // ----------------- 用户 cookie中是否存在uuid --------------
        String uuid = null;
    	Cookie myCookie = CookieUtils.findCookieByName(httpRequest, DpopSession.USER_DPOP_SESSION_ID);
    	if(myCookie != null) {
    		uuid = myCookie.getValue();
    	}
    	
        if(uuid != null) {
        	DpopSession session = DpopSessionFactory.getMySession(uuid);
        	Object userName = session.getAttribute("userName");
        	if(userName != null) {
        		// 用户已登录
        		chain.doFilter(request, response);
                return;
        	}
        }
        // ---------------------------------------------
        
        
        // 不在excludePath则走UUAP中的Auth验证过滤
    	authenticationFilter.doFilter(request, response, chain);
		
		return;
	}

	@Override
	public void destroy() {
		if(authenticationFilter != null) {
			authenticationFilter.destroy();
		}
	}
	
	/**
     * 将配置的excludePath转换成TreeSet结构
     * */
    private TreeSet<String> getExcludePathSet(String excludePaths) {
    	TreeSet<String> ret = new TreeSet<String>();
        if (excludePaths != null) {
            String[] paths = excludePaths.split(";");
            for (String p : paths) {
                if (p != null && p.length() > 0)
                	ret.add(p);
            }
        }
        
        return ret;
    }
    
}
