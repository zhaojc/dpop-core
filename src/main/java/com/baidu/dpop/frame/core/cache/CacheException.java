package com.baidu.dpop.frame.core.cache;

/**   
 * 缓存相关异常
 * 
 * @author cgd  
 * @date 2014年8月29日 下午3:01:06 
 */
public class CacheException extends RuntimeException {

	private static final long serialVersionUID = -3553760286606705644L;
	
	/**
	 * Default Constructor.
	 * */
	public CacheException() {}
	
	
	/**
	 * Cache Exception Constructor With Message.
	 * */
	public CacheException(String msg) {
		super(msg);
	}
	
	/**
	 * Cache Exception Constructor With Message and exception stacktrace.
	 * */
	public CacheException(String msg, Exception e) {
		super(msg, e);
	}

}
