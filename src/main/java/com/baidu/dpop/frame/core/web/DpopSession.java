package com.baidu.dpop.frame.core.web;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.Cookie;

/**   
 * DPOP Session Interface
 * @author cgd  
 * @date 2014年8月31日 下午4:42:54 
 */
public interface DpopSession {
	
	String USER_DPOP_SESSION_ID = "USER_DPOP_SESSION_ID";
	
	/**
	 * Session初始化相关
	 * */
	public void init();
	
	/**
	 * 获取session标识
	 * */
	public String getSessionId();
	
	/**
	 * 获取用户标识
	 * */
	public String getUUID();
	
	/**
	 * 获取Key相关Attribute
	 * */
	public Object getAttribute(String key) ;	
	
	/** 
	* 设置key对应的属性值 
	* @param name
	* @param value  参数
	*/
	public void setAttribute(String name, Serializable value) ;
	
	/**
	 * 清除Key匹配的Attribute数据
	 * */
	public void removeAttribute(String name) ;
	
	/**
	 * 获取session中的Map数据
	 * */
	Map<String, Object> getAll();
	
	/**
	 * 清除session Map中所有数据
	 * */
	public void removeAll();
}
