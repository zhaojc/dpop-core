package com.baidu.dpop.frame.core.cache.redis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.dpop.frame.core.cache.CacheException;
import com.baidu.dpop.frame.core.cache.CacheManager;

/**   
 * Redis的Cache Manager实现类
 * 
 * @author cgd  
 * @date 2014年8月29日 下午4:03:51 
 */
public class RedisCacheManager implements CacheManager {
	
	/** The Redis Clients Managered **/
	private List<RedisClient> clientList;
	
	/** 默认处理失败后的重试次数 **/
	private static final Integer DEFAULT_RETRY_TIMES = 2;
	
	/** 处理失败后的重试次数 **/
	private Integer reTryTimes = DEFAULT_RETRY_TIMES;

	
	/**
	 * 封装内部处理逻辑
	 * */
	private<T> T doProcess(RedisCallBack<T> callback, boolean isRead) {
		for(int i=0; i<reTryTimes; ++i) {
			// invoke callback method
			boolean isSuccess = callback.process(clientList, isRead);
			if(isSuccess) {
				return callback.getResult();
			}
		}
		// 处理失败，返回null
		return null;
	}
	
	/**
	 *  Set the Object as value of the key.
	 * 
	 * @param key
	 * @param valuse
	 */
	@Override
	public void set(final String key, final Object value) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						client.set(key, value);
						return true;
					} catch(Exception e) {
						throw new CacheException("SET failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "SET";
				}
			}, false);
		}
	}
	
	/**
	 *  Set the Object as value of the key.
	 * 
	 * @param key
	 * @param value
	 * @param expiration 超时时间
	 */
	@Override
	public void set(final String key, final Object value, final Integer expiration) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						client.set(key, value, expiration);
						return true;
					} catch(Exception e) {
						throw new CacheException("SET_WITH_EXPIRATION failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "SET_WITH_EXPIRATION";
				}
			}, false);
		}
	}
	
	/**
	 * Get the value of the specified key. 
	 * 
	 * @param key 
	 * @return 若不存在返回NULL
	 */
	@Override
	public Object get(final String key) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<Object>(){
				@Override
				protected Object callback(RedisClient client) {
					try {
						return client.get(key);
					} catch(Exception e) {
						throw new CacheException("GET failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "GET";
				}
			}, true);
		}
		return null;
	}
	
	/**
	 * 
	 * Test if the specified key exists.
	 * @param key
	 * @return
	 */
	@Override
	public boolean exists(final String key) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						return client.exists(key);
					} catch(Exception e) {
						throw new CacheException("EXISTS failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "EXISTS";
				}
			}, true);
		}
		return false;
	}
	
	/**
	 * Remove the specified keys.
	 * 
	 * @param key
	 */
	@Override
	public void delete(final String key) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						client.delete(key);
						return true;
					} catch(Exception e) {
						throw new CacheException("DELETE failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "DELETE";
				}
			}, false);
		}
	}
	
	/**
	 * Set a timeout on the specified key. <br/>
	 * After the timeout the key will be automatically deleted by the server.
	 * 
	 * @param key
	 * @param seconds 超时时间（单位秒）
	 */
	@Override
	public void expire(final String key, final int seconds) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						client.expire(key, seconds);
						return true;
					} catch(Exception e) {
						throw new CacheException("expire failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "expire";
				}
			}, false);
		}
	}
	
	/**
	 * If key holds a hash, retrieve the value associated to the specified field. <br/>
	 * If the field is not found or the key does not exist, a special 'nil' value is returned.
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * */
	@Override
	public Object hget(final String mapName, final String field) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<Object>(){
				@Override
				protected Object callback(RedisClient client) {
					try {
						return client.hget(mapName, field);
					} catch(Exception e) {
						throw new CacheException("HGET failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "HGET";
				}
			}, true);
		}
		return null;
	}
	
	
	/**
	 * Set the specified hash field to the specified value. <br/>
	 * If key does not exist, a new key holding a hash is created.
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * @param fieldValue value值
	 * */
	@Override
	public void hput(final String mapName, final String field, final Serializable fieldValue) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						client.hput(mapName, field, fieldValue);
						return true;
					} catch(Exception e) {
						throw new CacheException("HPUT failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "HPUT";
				}
			}, false);
		}
	}
	
	
	/**
	 * Remove the specified field from an hash stored at key. 
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * */
	@Override
	public void hdel(final String mapName, final String field) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						client.hdel(mapName, field);
						return true;
					} catch(Exception e) {
						throw new CacheException("HDEL failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "HDEL";
				}
			}, false);
		}
	}
	
	/**
	 * Test for existence of a specified field in a hash. 
	 * 
	 * @param key Map Name
	 * @param filed Map key name
	 * */
	@Override
	public boolean hExists(final String key, final String field) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						return client.hExists(key, field);
					} catch(Exception e) {
						throw new CacheException("hExists failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "hExists";
				}
			}, true);
		}
		
		return false;
	}
	
	/**
	 * Return the number of items in a hash.
	 * 
	 * @param mapName Map Name
	 * */
	@Override
	public Long hLen(final String mapName) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<Long>(){
				@Override
				protected Long callback(RedisClient client) {
					try {
						return client.hLen(mapName);
					} catch(Exception e) {
						throw new CacheException("HLEN failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "HLEN";
				}
			}, true);
		}
		
		return null;
	}
	
	/**
	 * Return all the fields in a hash. 
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * */
	@Override
	public Set<String> hKeys(final String mapName) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<Set<String>>(){
				@Override
				protected Set<String> callback(RedisClient client) {
					try {
						return client.hKeys(mapName);
					} catch(Exception e) {
						throw new CacheException("HKEYS failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "HKEYS";
				}
			}, true);
		}
		
		return null;
	}
	
	/**
	 * Return all the values in a hash. 
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * */
	@Override
	public List<Object> hValues(final String mapName) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<List<Object>>(){
				@Override
				protected List<Object> callback(RedisClient client) {
					try {
						return client.hValues(mapName);
					} catch(Exception e) {
						throw new CacheException("HVALUES failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "HVALUES";
				}
			}, true);
		}
		
		return null;
	}
	
	/**
	 * Set the respective fields to the respective values. <br/>
	 * HMSET replaces old values with new values. <br/>
	 * If key does not exist, a new key holding a hash is created.
	 * 
	 * @param mapName Map Name
	 * @param values 
	 * */
	public void hmSet(final String mapName, final Map<String, Serializable> values) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			this.doProcess(new RedisCallBack<Boolean>(){
				@Override
				protected Boolean callback(RedisClient client) {
					try {
						client.hmSet(mapName, values);
						return true;
					} catch(Exception e) {
						throw new CacheException("HMSET failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "HMSET";
				}
			}, false);
		}
	}
	
	/**
	 * Retrieve the values associated to the specified fields. <br/>
	 * If some of the specified fields do not exist, nil values are returned. <br/>
	 * Non existing keys are considered like empty hashes. 
	 * 
	 * @param mapName
	 * @param fileds
	 * */
	public List<Object> hmGet(final String mapName, final String... fields) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<List<Object>>(){
				@Override
				protected List<Object> callback(RedisClient client) {
					try {
						return client.hmGet(mapName, fields);
					} catch(Exception e) {
						throw new CacheException("HMGET failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "HMGET";
				}
			}, true);
		}
		
		return null;
	}
	
	
	/**
	 * Return all the fields and associated values in a hash. 
	 * 
	 * @param mapName Map Name
	 * */
	@Override
	public Map<String, Object> hGetAll(final String mapName) {
		if(CollectionUtils.isNotEmpty(clientList)) {
			return this.doProcess(new RedisCallBack<Map<String, Object>>(){
				@Override
				protected Map<String, Object> callback(RedisClient client) {
					try {
						return client.hGetAll(mapName);
					} catch(Exception e) {
						throw new CacheException("hGetAll failed in callback", e);
					}
				}
				@Override
				protected String getOpertionType() {
					return "hGetAll";
				}
			}, true);
		}
		
		return null;
	}
	
	/**
	 * Shut down the connection pool.
	 * */
	public void shutdown() {
		if(CollectionUtils.isNotEmpty(clientList)) {
			for(RedisClient client : clientList) {
				client.shutdown();
			}
		}
	}
	
	// --------------------------- get & set methods ----------------------------
	public void setClientList(List<RedisClient> clientList) {
		this.clientList = clientList;
	}
	public List<RedisClient> getClientList() {
		return clientList;
	}
	public Integer getReTryTimes() {
		return reTryTimes;
	}
	public void setReTryTimes(Integer reTryTimes) {
		this.reTryTimes = reTryTimes;
	}

}
