package com.baidu.dpop.frame.core.web;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.baidu.dpop.frame.core.cache.CacheManager;

/**   
 * DpopSession的Simple实现类
 * @author cgd  
 * @date 2014年8月31日 下午7:35:19 
 */
public class SimpleDpopSession implements DpopSession{
	
	// cache管理器
	private CacheManager cacheManager;
	
	// 用户标识
	private String uuid;
	
	// 区分各个用户对应的session
	private String sessionId;
	
	// 超时时间
	private Integer expiration = 0; 
	
	private static final String PRIMARY_SESSION_KEY = "DPOP_PRIMARY_SESSION_KEY_";
	
	/**
	 * Session初始化
	 * */
	@Override
	public void init() {
		// 存储session数据
		this.getCacheManager().hput(sessionId, "uuid", uuid);
		
		// 如果有配置超时时间
		if(expiration > 0) {
			this.getCacheManager().expire(sessionId, expiration);
		}
	}
	
	/**
	 * session构造函数
	 * 
	 * @param uuid 登录用户标识
	 * */
	public SimpleDpopSession(String uuid) {
		this.uuid = uuid;
		this.sessionId = PRIMARY_SESSION_KEY + uuid;
	}
	
	/**
	 * 获取session标识
	 * */
	@Override
	public String getSessionId() {
		return sessionId;
	}
	
	/**
	 * 获取用户标识
	 * */
	@Override
	public String getUUID() {
		return uuid;
	}
	

	/** 
	* 获取key对应的属性值
	* @param key
	* @return  参数
	*/
	@Override
	public Object getAttribute(String key) {
		return this.getCacheManager().hget(sessionId, key);
	}

	/** 
	* 设置key对应的属性值 
	* @param name
	* @param value  参数
	*/
	@Override
	public void setAttribute(String name, Serializable value) {
		this.getCacheManager().hput(sessionId, name, value);
		
	}

	/** 
	* 清除指定Name的Attribute
	* @param name  参数
	*/
	@Override
	public void removeAttribute(String name) {
		this.getCacheManager().hdel(sessionId, name);
		
	}

	/** 
	* 获取指定sessionID相关的所有属性
	* @return  参数
	*/
	@Override
	public Map<String, Object> getAll() {
		return this.getCacheManager().hGetAll(sessionId);
	}

	/** 
	* 清除session中所有属性
	*/
	@Override
	public void removeAll() {
		this.getCacheManager().delete(sessionId);
	}

	
	// ------------------------- get & set -------------------------
	public CacheManager getCacheManager() {
		return cacheManager;
	}
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	public Integer getExpiration() {
		return expiration;
	}
	public void setExpiration(Integer expiration) {
		this.expiration = expiration;
	}
	

}
