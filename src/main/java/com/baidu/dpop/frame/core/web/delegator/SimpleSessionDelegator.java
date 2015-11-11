package com.baidu.dpop.frame.core.web.delegator;

import com.baidu.dpop.frame.core.cache.CacheManager;
import com.baidu.dpop.frame.core.web.DpopSession;
import com.baidu.dpop.frame.core.web.SimpleDpopSession;

/**   
 * TODO (description)
 * @author cgd  
 * @date 2014年9月1日 下午3:30:08 
 */
public class SimpleSessionDelegator implements SessionDelegator {
	
	// cache管理器
	private CacheManager cacheManager;
	
	// 超时时间（单位：s）
	private Integer expirationSeconds = 0;

	/** 
	* 自定义Session获取
	* 
	* @param uuid
	* @return  DPOPSession
	*/
	@Override
	public DpopSession getSession(String uuid) {
		SimpleDpopSession session = new SimpleDpopSession(uuid);
		session.setCacheManager(cacheManager);
		session.setExpiration(expirationSeconds);
		
		// 相关初始化
		session.init();
		
		return session;
	}

	
	// --------------------------------------------------------
	public CacheManager getCacheManager() {
		return cacheManager;
	}
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	public Integer getExpirationSeconds() {
		return expirationSeconds;
	}
	public void setExpirationSeconds(Integer expirationSeconds) {
		this.expirationSeconds = expirationSeconds;
	}
	
}
