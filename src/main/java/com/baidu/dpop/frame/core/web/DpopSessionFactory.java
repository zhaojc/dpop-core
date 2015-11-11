package com.baidu.dpop.frame.core.web;

import com.baidu.dpop.frame.core.context.SpringContextUtil;
import com.baidu.dpop.frame.core.web.delegator.SessionDelegator;

/**   
 * DpopSession生成的工厂类
 * 
 * @author cgd  
 * @date 2014年9月1日 下午5:18:58 
 */
public class DpopSessionFactory {
	
	// session获取代理类
	private static SessionDelegator sessionDelegator = null;
	
	/**
	 * 校验是否存在session代理类，若不存在则从容器中加载
	 * */
	private static void checkSessionDelegator() {
		if(sessionDelegator == null) {
			sessionDelegator = SpringContextUtil.getBean(SessionDelegator.class);
		}
	}
	
	/**
	 * 获取指定ID的session
	 * @param uuid 当前用户登录的uuid
	 * */
	public static DpopSession getMySession(String uuid) {
		checkSessionDelegator();
		
		// 如果session代理类存在
		if(sessionDelegator != null) {
			return sessionDelegator.getSession(uuid);
		}
		return null;
	}
	
	/**
	 * 清除指定id的session
	 * @param uuid 当前用户登录的uuid
	 * */
	public static void removeMySession(String uuid) {
		checkSessionDelegator();
		
		if(uuid != null && sessionDelegator != null) {
			DpopSession session = getMySession(uuid);
			if(session != null) {
				session.removeAll();
			}
		}
	}
 
}
