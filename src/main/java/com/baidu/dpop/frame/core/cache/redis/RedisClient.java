package com.baidu.dpop.frame.core.cache.redis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
//import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.baidu.dpop.frame.core.cache.CacheClient;
import com.baidu.dpop.frame.core.cache.CacheException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.SafeEncoder;

import java.io.ByteArrayInputStream;

/**
 * Redis客户端
 * 
 * @author cgd
 * @date 2014年8月29日 下午4:50:23
 */
public class RedisClient implements CacheClient, DisposableBean {

    // log4j日志输出
    private static final Logger LOG = Logger.getLogger(RedisClient.class);

    private String cacheName = "default";

    /** 服务器名 **/
    private String redisServer;

    /** 权限认证 **/
    private String redisAuthKey;

    /** redis连接池 **/
    private JedisPool jedisPool;

    /** redis server端口号 **/
    private Integer port = Protocol.DEFAULT_PORT;

    /** 连接等待时间 **/
    private Integer timeout = Protocol.DEFAULT_TIMEOUT;

    /**
     * if maxIdle == 0, ObjectPool has 0 size pool
     */
    private Integer maxIdle = GenericObjectPool.DEFAULT_MAX_IDLE;

    /** 调用最长等待时间 **/
    private Long maxWait = GenericObjectPool.DEFAULT_MAX_WAIT;

    public static void main(String[] args) {
        RedisClient client = new RedisClient("10.87.131.30", 8379, "jinbao_innerapi_pwd");
        client.add("cgd_test_name", "shine");
        System.out.println(client.get("cgd_test_name"));
    }

    /**
     * Redis服务连接配置初始化
     */
    @PostConstruct
    public void init() {
        // 初始化连接配置信息
        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();

        // maxIdle为负数时，sop中不对pool size大小做限制，此处做限制，防止保持过多空闲redis连接
        if (this.maxIdle >= 0) {
            poolConfig.maxIdle = this.maxIdle;
        }

        // 等待时间
        poolConfig.maxWait = this.maxWait;

        // 初始化Redis服务器的连接池（是否需要密码验证）
        if (StringUtils.isNotEmpty(redisAuthKey)) {
            this.jedisPool = new JedisPool(poolConfig, redisServer, port, timeout, redisAuthKey);
        } else {
            this.jedisPool = new JedisPool(poolConfig, redisServer, port, timeout);
        }
    }

    /**
     * 常用构造函数
     * 
     * @param server Redis server name
     * @param server Redis server port
     * */
    public RedisClient(String server, Integer port) {
        this.redisServer = server;
        this.port = port;

        // 初始化
        this.init();
    }

    public RedisClient(String server, Integer port, String pwd) {
        this.redisServer = server;
        this.port = port;
        this.redisAuthKey = pwd;

        // 初始化
        this.init();
    }

    public RedisClient() {
        this.init();
    }

    /**
     * Get the value of the specified key.
     * 
     * @param key
     * @return 若不存在返回NULL
     * @throws Exception
     */
    public Object get(String key) {
        byte[] data = null;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            long begin = System.currentTimeMillis();
            data = jedis.get(SafeEncoder.encode(key));
            long end = System.currentTimeMillis();
            LOG.info("getValueFromCache spends：" + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            // do jedis.quit() and jedis.disconnect()
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis Get Exception", e);
            throw new CacheException("Redis Get Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }

        return this.deserialize(data);
    }

    /**
     * Set the string value as value of the key with expiration.
     * 
     * @param key
     * @param value
     * @param expiration 超时时间（单位是s）
     * @return 失败返回false
     */
    public void set(String key, Object value, Integer expiration) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();

            long begin = System.currentTimeMillis();
            if (expiration > 0) {
                jedis.setex(SafeEncoder.encode(key), expiration, serialize(value));
            } else {
                jedis.set(SafeEncoder.encode(key), serialize(value));
            }
            long end = System.currentTimeMillis();
            LOG.info("set key:" + key + " spends：" + (end - begin) + " millionseconds.");
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis Set Exception", e);
            throw new CacheException("Redis Set Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }

    }

    /**
     * Set the string value as value of the key.
     * 
     * @param key
     * @param value
     * @return 存储失败返回false
     * @throws Exception
     */
    public void set(String key, Object value) {
        this.set(key, value, -1);
    }

    /**
     * SETNX works exactly like SET with the only difference that if the key already exists no operation is performed. <br/>
     * SETNX actually means "SET if Not eXists". <br/>
     * 
     * @param key
     * @param value
     * @param expiration 超时时间
     * @return false if redis did not execute the option
     * @throws Exception
     */
    public boolean add(String key, Object value, Integer expiration) {
        Jedis jedis = null;

        try {

            jedis = this.jedisPool.getResource();
            long begin = System.currentTimeMillis();
            // 操作setnx与expire成功返回1，失败返回0，仅当均返回1时，实际操作成功
            Long result = jedis.setnx(SafeEncoder.encode(key), serialize(value));
            if (expiration > 0) {
                result = result & jedis.expire(key, expiration);
            }
            long end = System.currentTimeMillis();
            if (result == 1L) {
                LOG.info("add key:" + key + " spends：" + (end - begin) + " millionseconds.");
            } else {
                LOG.info("add key: " + key + " failed, key has already exists! ");
            }

            return result == 1L;
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis Add Exception", e);
            throw new CacheException("Redis Add Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * add if not exists without expiration time.
     * 
     * @param key
     * @param value
     * @return false if redis did not execute the option
     * @throws Exception
     */
    public boolean add(String key, Object value) {
        return this.add(key, value, -1);
    }

    /**
     * 
     * Test if the specified key exists.
     * 
     * @param key
     * @return
     * @throws Exception
     */
    public boolean exists(String key) {
        boolean isExist = false;
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            isExist = jedis.exists(SafeEncoder.encode(key));

        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis exists Exception", e);
            throw new CacheException("Redis exists Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
        return isExist;
    }

    /**
     * Remove the specified keys.
     * 
     * @param key
     * @return false if redis did not execute the option
     */
    public void delete(String key) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            jedis.del(SafeEncoder.encode(key));
            LOG.info("delete key:" + key);
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis delete Exception", e);
            throw new CacheException("Redis delete Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
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
    public void expire(String key, int seconds) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            jedis.expire(SafeEncoder.encode(key), seconds);
            LOG.info("expire key:" + key + " time after " + seconds + " seconds.");
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis expire Exception", e);
            throw new CacheException("Redis expire Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * 
     * Delete all the keys of all the existing databases, not just the currently selected one. <br/>
     * This command never fails. <br/>
     * (Remove all the data, so be careful.)
     */
    /*
     * public void flushall(){ Jedis jedis = null; try{ jedis = this.jedisPool.getResource(); jedis.flushAll();
     * LOG.info("redis client name: " + this.getCacheName() + " flushall."); } catch (Exception e) {
     * this.jedisPool.returnBrokenResource(jedis); LOG.error("Redis expire Exception", e); throw new
     * CacheException("Redis expire Exception", e); } finally { if(jedis != null){ this.jedisPool.returnResource(jedis);
     * } } }
     */

    /**
     * Set the specified hash field to the specified value. <br/>
     * If key does not exist, a new key holding a hash is created.
     * 
     * @param key Map Name
     * @param filed Map key name
     * @param fieldValue value值
     * */
    public void hput(String key, String field, Serializable fieldValue) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            jedis.hset(SafeEncoder.encode(key), SafeEncoder.encode(field), serialize(fieldValue));
            LOG.info("hset key:" + key + " field:" + field);
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hput Exception", e);
            throw new CacheException("Redis hput Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * If key holds a hash, retrieve the value associated to the specified field. <br/>
     * If the field is not found or the key does not exist, a special 'nil' value is returned.
     * 
     * @param key Map Name
     * @param filed Map key name
     * */
    public Object hget(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            byte[] value = jedis.hget(SafeEncoder.encode(key), SafeEncoder.encode(field));
            LOG.info("hget key:" + key + " field:" + field);

            return deserialize(value);
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hget Exception", e);
            throw new CacheException("Redis hget Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Remove the specified field from an hash stored at key.
     * 
     * @param key Map Name
     * @param filed Map key name
     * */
    public boolean hdel(String key, String field) {
        Jedis jedis = null;
        try {

            jedis = this.jedisPool.getResource();
            long value = jedis.hdel(SafeEncoder.encode(key), SafeEncoder.encode(field));
            LOG.info("hget key:" + key + " field:" + field);

            return value == 1;
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hdel Exception", e);
            throw new CacheException("Redis hdel Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Return all the fields in a hash.
     * 
     * @param key Map Name
     * @param filed Map key name
     * */
    public Set<String> hKeys(String key) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Set<byte[]> hkeys = jedis.hkeys(SafeEncoder.encode(key));
            LOG.info("hkeys key:" + key);
            if (CollectionUtils.isEmpty(hkeys)) {
                return new HashSet<String>(1);
            } else {
                Set<String> keys = new HashSet<String>(hkeys.size());
                for (byte[] bb : hkeys) {
                    keys.add(SafeEncoder.encode(bb));
                }
                return keys;
            }
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hKeys Exception", e);
            throw new CacheException("Redis hKeys Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Return all the values in a hash.
     * 
     * @param key Map Name
     * @param filed Map key name
     * */
    public List<Object> hValues(String key) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            List<byte[]> hvals = jedis.hvals(SafeEncoder.encode(key));
            LOG.info("hvals key:" + key);
            if (CollectionUtils.isEmpty(hvals)) {
                return new ArrayList<Object>(1);
            } else {
                List<Object> ret = new ArrayList<Object>(hvals.size());
                for (byte[] bb : hvals) {
                    ret.add(deserialize(bb));
                }
                return ret;
            }
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hValues Exception", e);
            throw new CacheException("Redis hValues Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Test for existence of a specified field in a hash.
     * 
     * @param key Map Name
     * @param filed Map key name
     * */
    public boolean hExists(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            boolean ret = jedis.hexists(SafeEncoder.encode(key), SafeEncoder.encode(field));
            LOG.info("hexists key:" + key + " field:" + field);

            return ret;
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hExists Exception", e);
            throw new CacheException("Redis hExists Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Return the number of items in a hash.
     * 
     * @param key Map Name
     * */
    public long hLen(String key) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            long ret = jedis.hlen(SafeEncoder.encode(key));
            LOG.info("hlen key:" + key);

            return ret;
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hLen Exception", e);
            throw new CacheException("Redis hLen Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Return all the fields and associated values in a hash.
     * 
     * @param mapName Map Name
     * */
    @Override
    public Map<String, Object> hGetAll(String mapName) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            Map<byte[], byte[]> hgetAll = jedis.hgetAll(SafeEncoder.encode(mapName));
            LOG.info("hgetAll key:" + mapName);

            return decodeMap(hgetAll);
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hGetAll Exception", e);
            throw new CacheException("Redis hGetAll Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Set the respective fields to the respective values. <br/>
     * HMSET replaces old values with new values. <br/>
     * If key does not exist, a new key holding a hash is created.
     * 
     * @param key Map Name
     * @param values
     * */
    public void hmSet(String key, Map<String, Serializable> values) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            jedis.hmset(SafeEncoder.encode(key), encodeMap(values));
            LOG.info("hmSet key:" + key + " field:" + values.keySet());
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hmSet Exception", e);
            throw new CacheException("Redis hmSet Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Retrieve the values associated to the specified fields. <br/>
     * If some of the specified fields do not exist, nil values are returned. <br/>
     * Non existing keys are considered like empty hashes.
     * 
     * @param key
     * @param fileds
     * */
    public List<Object> hmGet(String key, String...fields) {
        Jedis jedis = null;
        try {
            jedis = this.jedisPool.getResource();
            List<byte[]> hmget = jedis.hmget(SafeEncoder.encode(key), encodeArray(fields));
            LOG.info("hmGet key:" + key + " fields:" + Arrays.toString(fields));
            if (CollectionUtils.isEmpty(hmget)) {
                return new ArrayList<Object>(1);
            } else {
                List<Object> ret = new ArrayList<Object>(hmget.size());
                for (byte[] bb : hmget) {
                    ret.add(deserialize(bb));
                }
                return ret;
            }
        } catch (Exception e) {
            this.jedisPool.returnBrokenResource(jedis);
            LOG.error("Redis hmGet Exception", e);
            throw new CacheException("Redis hmGet Exception", e);
        } finally {
            if (jedis != null) {
                this.jedisPool.returnResource(jedis);
            }
        }
    }

    /**
     * Decode data from Map<byte[], byte[]> to Map<String, Object>
     * 
     * @param Map<byte[], byte[]>
     * 
     * */
    private Map<String, Object> decodeMap(final Map<byte[], byte[]> values) {
        if (MapUtils.isEmpty(values)) {
            return Collections.emptyMap();
        }
        Map<byte[], byte[]> copy = new HashMap<byte[], byte[]>(values);
        Iterator<Entry<byte[], byte[]>> iterator = copy.entrySet().iterator();
        Map<String, Object> ret = new HashMap<String, Object>();
        while (iterator.hasNext()) {
            Entry<byte[], byte[]> next = iterator.next();
            ret.put(SafeEncoder.encode(next.getKey()), deserialize(next.getValue()));
        }

        return ret;
    }

    /**
     * Encode data from Map<String, Serializable> to Map<byte[], byte[]>
     * 
     * @param Map<byte[], byte[]>
     * 
     * */
    private Map<byte[], byte[]> encodeMap(final Map<String, Serializable> values) {
        if (MapUtils.isEmpty(values)) {
            return Collections.emptyMap();
        }
        Map<String, Serializable> copy = new HashMap<String, Serializable>(values);
        Iterator<Entry<String, Serializable>> iterator = copy.entrySet().iterator();
        Map<byte[], byte[]> ret = new HashMap<byte[], byte[]>();
        while (iterator.hasNext()) {
            Entry<String, Serializable> next = iterator.next();
            ret.put(SafeEncoder.encode(next.getKey()), serialize(next.getValue()));
        }

        return ret;
    }

    /**
     * Encode data from String[] to byte[][].
     * 
     * */
    private byte[][] encodeArray(final String[] array) {
        if (array == null || array.length == 0) {
            return new byte[0][0];
        }
        int len = array.length;
        List<byte[]> list = new ArrayList<byte[]>(len);
        for (int i = 0; i < len; i++) {
            list.add(SafeEncoder.encode(array[i]));
        }
        return list.toArray(new byte[len][0]);
    }

    /**
     * Get the bytes representing the given serialized object.
     */
    protected byte[] serialize(Object o) {
        if (o == null) {
            // throw new NullPointerException("Can't serialize null");
            return new byte[0];
        }
        byte[] rv = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(o);
            os.close();
            bos.close();
            rv = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object", e);
        }
        return rv;
    }

    /**
     * Get the object represented by the given serialized bytes.
     */
    protected Object deserialize(byte[] in) {
        Object rv = null;
        try {
            if (in != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(in);
                ObjectInputStream is = new ObjectInputStream(bis);
                rv = is.readObject();
                is.close();
                bis.close();
            }
        } catch (IOException e) {
            LOG.error("Caught IOException decoding %d bytes of data", e);
        } catch (ClassNotFoundException e) {
            LOG.error("Caught CNFE decoding %d bytes of data", e);
        }
        return rv;
    }

    /**
     * Shut down the connection pool.
     * */
    public void shutdown() {
        this.jedisPool.destroy();
    }

    /**
     * Shut down the connection pool.
     * */
    public void destroy() {
        this.jedisPool.destroy();
    }

    // -------------------------- get & set method -----------------------
    public String getRedisServer() {
        return redisServer;
    }

    public void setRedisServer(String redisServer) {
        this.redisServer = redisServer;
    }

    public String getRedisAuthKey() {
        return redisAuthKey;
    }

    public void setRedisAuthKey(String redisAuthKey) {
        this.redisAuthKey = redisAuthKey;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
