package com.baidu.dpop.frame.core.user;

import com.baidu.dpop.frame.core.constant.DpopConstants;
import com.baidu.dpop.frame.core.context.SpringContextUtil;
import com.baidu.dpop.frame.core.util.ThreadLocalInfo;


/**
 * 获取当前登录用户相关信息
 * 
 * @author cgd
 * 
 * */
public class UserInfoHelper {
	
	
	/**
	 * 获取当前登录用户的用户信息
	 * 
	 * @return UserName, 未获取到则返回null
	 * */
	public static String getCurrentUserName() {
		Object userName = ThreadLocalInfo.getAttribute(DpopConstants.DPOP_USER_NAME_ATTRIBUTE);
		return userName == null ? null : userName.toString();
	}
	
	/**
	 * 获取当前登录用户的UUID
	 * 
	 * @return uuid, 未获取到则返回null
	 * */
	public static String getCurrentUserUUID() {
		Object uuid = ThreadLocalInfo.getAttribute(DpopConstants.DPOP_USER_UUID_ATTRIBUTE);
		return uuid == null ? null : uuid.toString();
	}
	
}
