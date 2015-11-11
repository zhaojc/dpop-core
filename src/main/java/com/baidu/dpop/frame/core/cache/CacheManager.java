package com.baidu.dpop.frame.core.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**   
 * Cache Manager Interface.
 * 
 * @author cgd  
 * @date 2014年8月29日 下午3:39:34 
 */
public interface CacheManager {
	/**
	 * Get the value of the specified key. 
	 * 
	 * @param key 
	 * @return 若不存在返回NULL
	 */
	public Object get(String key);
	
	/**
	 *  Set the Object as value of the key.
	 * 
	 * @param key
	 * @param valuse
	 */
	public void set(String key, Object value);
	
	/**
	 * Set the string value as value of the key with expiration.
	 * 
	 * @param key
	 * @param value
	 * @param expiration 超时时间
	 */
	public void set(String key, Object value, Integer expiration);
	
	/**
	 * 
	 * Test if the specified key exists.
	 * @param key
	 * @return
	 */
	public boolean exists(String key);
	
	/**
	 * Remove the specified keys.
	 * 
	 * @param key
	 */
	public void delete(String key);
	
	/**
	 * Set a timeout on the specified key. <br/>
	 * After the timeout the key will be automatically deleted by the server.
	 * 
	 * @param key
	 * @param seconds 超时时间（单位秒）
	 */
	public void expire(String key, int seconds);
	
	/**
	 * If key holds a hash, retrieve the value associated to the specified field. <br/>
	 * If the field is not found or the key does not exist, a special 'nil' value is returned.
	 * 
	 * @param key Map Name
	 * @param filed Map key name
	 * */
	public Object hget(String key, String field);
	
	
	/**
	 * Set the specified hash field to the specified value. <br/>
	 * If key does not exist, a new key holding a hash is created.
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * @param fieldValue value值
	 * */
	public void hput(String mapName, String field, Serializable fieldValue);
	
	/**
	 * Remove the specified field from an hash stored at key. 
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * */
	public void hdel(String mapName, String field);
	
	/**
	 * Test for existence of a specified field in a hash. 
	 * 
	 * @param key Map Name
	 * @param filed Map key name
	 * */
	public boolean hExists(String key, String field);
	
	/**
	 * Return the number of items in a hash.
	 * 
	 * @param mapName Map Name
	 * */
	public Long hLen(String mapName);
	
	/**
	 * Return all the fields in a hash. 
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * */
	public Set<String> hKeys(String mapName);
	
	/**
	 * Return all the values in a hash. 
	 * 
	 * @param mapName Map Name
	 * @param filed Map key name
	 * */
	public List<Object> hValues(String mapName);
	
	/**
	 * Set the respective fields to the respective values. <br/>
	 * HMSET replaces old values with new values. <br/>
	 * If key does not exist, a new key holding a hash is created.
	 * 
	 * @param mapName Map Name
	 * @param values 
	 * */
	public void hmSet(String mapName, Map<String, Serializable> values);
	
	/**
	 * Retrieve the values associated to the specified fields. <br/>
	 * If some of the specified fields do not exist, nil values are returned. <br/>
	 * Non existing keys are considered like empty hashes. 
	 * 
	 * @param mapName
	 * @param fileds
	 * */
	public List<Object> hmGet(String mapName, String... fields);
	
	/**
	 * Return all the fields and associated values in a hash. 
	 * 
	 * @param mapName Map Name
	 * */
	public Map<String, Object> hGetAll(String mapName);
	
	/**
	 * Shut down the connection pool.
	 * */
	public void shutdown();
}
