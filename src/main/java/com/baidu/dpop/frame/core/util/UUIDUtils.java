package com.baidu.dpop.frame.core.util;

import java.util.UUID;

/**   
 * UUID获取工具类
 * @author cgd  
 * @date 2014年9月1日 下午6:20:00 
 */
public class UUIDUtils {
	
	/**
	 * 获取一个随机的UUID
	 * */
	public static String getUUID() {
		return UUID.randomUUID().toString();
	}
	
}
