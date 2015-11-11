package com.baidu.dpop.frame.core.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;


/**   
 * UUID获取工具类
 * @author cgd  
 * @date 2014年9月1日 下午6:20:00 
 */
public class CookieUtils {
	
	/**
	 * 查找指定name的cookie
	 * 
	 * @param cookieName cookie name
	 * */
	public static Cookie findCookieByName(HttpServletRequest request, String cookieName) {
		Assert.notNull(request);
		Assert.notNull(cookieName);
		
		Cookie ret = null;
		Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0) {
        	for(Cookie item : cookies) {
        		if(cookieName.equals(item.getName())) {
        			ret = item;
        			break;
        		}
        	}
        }
		return ret;
	}
	
	
	/**
	 * 创建一个新的cookie
	 * 
	 * @param name cookie name
	 * @param value cookie value
	 * @param domain cookie所属的域
	 * @param path cookie存储path
	 * @param maxage cookie存活时间
	 * */
	public static Cookie createNewCookie(String name, String value, String domain, String path, Integer maxage) {
		Assert.notNull(name);
		Assert.notNull(value);
		
		Cookie ret = new Cookie(name, value);
		// set cookie所属的域
		if(domain != null) {
			ret.setDomain(domain);
		}
		// set cookie存储path
		if(path != null) {
			ret.setPath(path);
		}
		// 设置cookie存活时间（默认-1，浏览器关闭时清空）
		if(maxage == null) {
			maxage = -1;
		}
		ret.setMaxAge(maxage);
		
		return ret;
	}
	
	/**
	 * 创建一个新的cookie
	 * 
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxage cookie存活时间
	 * */
	public static Cookie createNewCookie(String name, String value, Integer maxage) {
		return createNewCookie(name, value, null, null, maxage);
	}
	
	/**
	 * 创建一个新的cookie
	 * 
	 * @param name cookie name
	 * @param value cookie value
	 * */
	public static Cookie createNewCookie(String name, String value) {
		return createNewCookie(name, value, null, null, null);
	}
}
