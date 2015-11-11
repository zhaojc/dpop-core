package com.baidu.dpop.frame.core.util;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * Save Dpop ThreadLocal Infos
 * @author cgd
 * 
 * */
public class ThreadLocalInfo {
	
	/** 保存当前访问用户thread相关信息 **/
	private static final ThreadLocal<Map<String, Object>> curUserInfo 
						= new ThreadLocal<Map<String, Object>>();
	
	
	/**
	 * 获取指定名称的属性值
	 * 
	 * @param attributeName 属性名称
	 * */
	public static Object getAttribute(String attributeName) {
		Map<String, Object> data = curUserInfo.get();
		if(data != null) {
			return data.get(attributeName);
		}
		return null;
	}
	
	/**
	 * 获取指定名称的属性值
	 * 
	 * @param attributeName 属性名称
	 * @param value 属性值
	 * */
	public static void setAttribute(String attributeName, Object value) {
		Map<String, Object> data = curUserInfo.get();
		if(data == null) {
			data = new HashMap<String, Object>();
		} 
		data.put(attributeName, value);
		curUserInfo.set(data);
	}
	
	/**
	 * 清除用户相关所有信息
	 * */
	public static void removeAll() {
		curUserInfo.remove();
	}
}
