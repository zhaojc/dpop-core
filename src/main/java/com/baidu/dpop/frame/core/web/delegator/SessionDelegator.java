package com.baidu.dpop.frame.core.web.delegator;

import com.baidu.dpop.frame.core.web.DpopSession;


/**   
 * Session生成的代理类
 * @author cgd  
 * @date 2014年9月1日 下午3:26:56 
 */
public interface SessionDelegator {

	/**
	 * 自定义Session获取
	 * 
	 * @param sessionId 
	 * */
	public DpopSession getSession(String sessionId);
}
