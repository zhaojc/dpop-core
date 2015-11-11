package com.baidu.dpop.frame.core.cache.redis;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**   
 * Redis abstract Callback class
 * 
 * @author cgd  
 * @date 2014年8月29日 下午4:30:23 
 */
public abstract class RedisCallBack<T> {
	
	/** 返回结果 **/
	private T result;
	
	/**
	 * callback method.
	 * 
	 * @param  client Redis Client
	 * */
	protected abstract T callback(RedisClient client);
	
	/**
	 * 获取操作类型（get, set, delete, hget ... ）
	 * */
	protected abstract String getOpertionType();
	
	
	/**
	 * RedisManager 读写数据处理逻辑
	 * 
	 * @param client
	 * @param read Read data from Redis
	 * */
	public boolean process(List<RedisClient> clientList, boolean read) {
		boolean success = false;
		if(CollectionUtils.isNotEmpty(clientList)) {
			// write: 所有client必须做写入，read: 一台client读取到数据即可
			for(RedisClient client : clientList) {
				// invoke callback method
				T ret = callback(client);
				if(ret != null) {
					this.setResult(ret);
				}
				// read data from redis
				if(read) {
					if(ret != null) return true;
				}
				success = success || true;
			}
		}
		return success;
	}


	// -------------------------------------------------
	public T getResult() {
		return result;
	}
	public void setResult(T result) {
		this.result = result;
	}
	
}
