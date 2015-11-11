package com.baidu.dpop.frame.core.filter.session.redis;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.log4j.Logger;

import com.baidu.dpop.frame.core.filter.session.EmptyHttpSessionContext;

import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * 基于Redis的HttpSession实现。
 *
 * <p>
 *     <h3>会话迁移（session migration）</h3>
 *     由于使用Redis存储会话，实际的会话属性会被序列化、反序列化至Redis服务器中，等价于一次会话迁移。
 *     因此，对于实现{@link HttpSessionActivationListener}的会话属性，每一次调用{@link #getAttribute(String)}、
 *     {@link #setAttribute(String, Object)}时都会触发其对应的事件。
 * </p>
 *
 * @author jiwenhao
 */
class RedisHttpSession implements HttpSession {

    private static final String META_CREATION_TIME = "__meta__creation_time";
    private static final String META_LAST_ACCESSED_TIME = "__meta__last_accessed_time";

    private static final Long NEVER_BEEN_ACCESSED = 0L;

    private final Logger logger = Logger.getLogger(this.getClass());

    private final JedisPool jedisPool;
    private final RedisValueSerDe redisValueSerDe = new Base64RedisValueSerDe();

    private String sessionId;
    private String keyWithPrefix;
    private final boolean create;

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private int interval;

    private volatile boolean valid = true;

    /**
     * 创建一个新的Redis会话。
     *
     * @param jedisPool Redis连接池
     * @param interval 会话的有效时间（单位为秒，若为负数，则不失效）
     * @param keyPrefix Redis会话标识前缀
     * @param request 当前会话的原始请求（即在包装之前）
     * @param response 当前会话的原始响应
     */
    RedisHttpSession(JedisPool jedisPool, final int interval, String keyPrefix, HttpServletRequest request,
                     HttpServletResponse response) {
        this.jedisPool = jedisPool;
        this.interval = interval;

        this.request = request;
        this.response = response;

        final String normalizedKeyPrefix = (keyPrefix == null) ? "" : keyPrefix;
        create = true;

        execute(new JedisCallBack<Void>() {
            @Override
            public Void execute(Jedis jedis) {
                String sessionId;
                do {
                    sessionId = UUID.randomUUID().toString();
                } while (jedis.exists(normalizedKeyPrefix + sessionId));
                RedisHttpSession.this.sessionId = sessionId;

                keyWithPrefix = normalizedKeyPrefix + sessionId;
                initializeMetaValues(jedis);
                return null;
            }
        });
    }

    /**
     * 恢复一个已有的Redis会话。若该会话不存在，则创建一个新的。新的会话标识不参考传入的标识，将会重新分配。
     *
     * @param sessionId 会话的标识
     * @param jedisPool Redis连接池
     * @param interval 会话的有效时间（单位为秒，若为负数，则不失效），会覆盖原有的有效时间。
     * @param keyPrefix Redis会话标识前缀
     * @param request 当前会话的原始请求（即在包装之前）
     * @param response 当前会话的原始响应
     */
    RedisHttpSession(final String sessionId, JedisPool jedisPool, final int interval, String keyPrefix,
                     HttpServletRequest request, HttpServletResponse response) {
        this.jedisPool = jedisPool;
        this.interval = interval;

        this.request = request;
        this.response = response;

        final String normalizedKeyPrefix = (keyPrefix == null) ? "" : keyPrefix;

        this.create = execute(new JedisCallBack<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                boolean create;
                if (!jedis.exists(normalizedKeyPrefix + sessionId)) {
                    String generatedSessionId;
                    do {
                        generatedSessionId = UUID.randomUUID().toString();
                    } while (jedis.exists(generatedSessionId + sessionId));
                    create = true;
                } else {
                    create = false;
                }
                RedisHttpSession.this.sessionId = sessionId;
                RedisHttpSession.this.keyWithPrefix = normalizedKeyPrefix + sessionId;

                if (interval >= 0) {
                    jedis.expire(keyWithPrefix, interval);
                }

                if (create) {
                    initializeMetaValues(jedis);
                }
                return create;
            }
        });
    }

    /**
     * 初始化基础数据，并且保持至redis中
     *
     * @param jedis redis连接
     */
    private void initializeMetaValues(Jedis jedis) {
        long now = System.currentTimeMillis();
        Map<String, String> meta = new HashMap<String, String>();
        meta.put(META_CREATION_TIME, serializeObject(now));
        meta.put(META_LAST_ACCESSED_TIME, serializeObject(NEVER_BEEN_ACCESSED));
        jedis.hmset(keyWithPrefix, meta);
        if (interval >= 0) {
            jedis.expire(keyWithPrefix, interval);
        }
    }

    @Override
    public long getCreationTime() {
        String creationTimeMd5 = execute(new JedisHgetCallBack(META_CREATION_TIME));
        Object deserialized = deserializeObject(creationTimeMd5);
        if (deserialized == null) {
            logger.warn("failed to retrieve creation time from redis");
            return 0L;
        }
        return (Long) deserialized;
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public long getLastAccessedTime() {
        String lastAccessed = execute(new JedisHgetCallBack(META_LAST_ACCESSED_TIME));
        Object deserialized = deserializeObject(lastAccessed);
        if (deserialized == null) {
            logger.warn("failed to retrieve last accessed time from redis");
            return 0L;
        }
        return (Long) deserialized;
    }

    @Override
    public ServletContext getServletContext() {
        return request.getSession().getServletContext();
    }

    @Override
    public void setMaxInactiveInterval(final int interval) {
        execute(new JedisCallBack<Void>() {
            @Override
            public Void execute(Jedis jedis) {
                RedisHttpSession.this.interval = interval;
                if (interval < 0) {
                    jedis.persist(keyWithPrefix);
                } else {
                    jedis.expire(keyWithPrefix, interval);
                }
                return null;
            }
        });
    }

    @Override
    public int getMaxInactiveInterval() {
        return interval;
    }

    @Override
    @SuppressWarnings("deprecation")
    public HttpSessionContext getSessionContext() {
        return new EmptyHttpSessionContext();
    }

    @Override
    public Object getAttribute(String name) {
        ensureValidSession();
        if (name == null) {
            throw new NullPointerException();
        }

        String attributeInMD5 = execute(new JedisHgetCallBack(name));
        if (attributeInMD5 == null) {
            return null;
        }

        Object value = deserializeObject(attributeInMD5);

        if (value instanceof HttpSessionActivationListener) {
            fireActivateEvent((HttpSessionActivationListener) value, name, value);
        }
        return value;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        ensureValidSession();
        Map<String, String> all = execute(new JedisCallBack<Map<String, String>>() {
            @Override
            public Map<String, String> execute(Jedis jedis) {
                return jedis.hgetAll(keyWithPrefix);
            }
        });
        return Collections.enumeration(all.keySet());
    }

    @Override
    @SuppressWarnings("deprecation")
    public String[] getValueNames() {
        ensureValidSession();
        Map<String, String> all = execute(new JedisCallBack<Map<String, String>>() {
            @Override
            public Map<String, String> execute(Jedis jedis) {
                return jedis.hgetAll(keyWithPrefix);
            }
        });
        return all.keySet().toArray(new String[all.size()]);
    }

    @Override
    public void setAttribute(final String name, Object value) {
        ensureValidSession();
        if (name == null) {
            throw new NullPointerException();
        }
        if (value == null) {
            this.removeAttribute(name);
            return;
        }

        if (value instanceof HttpSessionActivationListener) {
            firePassivateEvent((HttpSessionActivationListener) value, name, value);
        }

        final String md5Encoded = serializeObject(value);
        String unboundEncoded = execute(new JedisCallBack<String>() {
            @Override
            public String execute(Jedis jedis) {
                String unboundEncoded = jedis.hget(keyWithPrefix, name);
                jedis.hset(keyWithPrefix, name, md5Encoded);
                return unboundEncoded;
            }
        });

        if (unboundEncoded != null) {
            Object unbound = deserializeObject(unboundEncoded);
            if (unbound == value) { // 同一对象重复设置（由于依赖于序列化接口，对象引用通常不同，很少发生）
                return;
            }
            if (unbound != null && unbound instanceof HttpSessionBindingListener) {
                fireUnboundEvent((HttpSessionBindingListener) unbound, name, unbound);
            }
        }
        if (value instanceof HttpSessionBindingListener) {
            fireBoundEvent((HttpSessionBindingListener) value, name, value);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void putValue(String name, Object value) {
        this.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        ensureValidSession();
        if (name == null) {
            throw new NullPointerException();
        }
        String unboundEncoded = execute(new JedisHgetAndHdelCallback(name));

        if (unboundEncoded != null) {
            Object unbound = deserializeObject(unboundEncoded);
            if (unbound != null && unbound instanceof HttpSessionBindingListener) {
                fireUnboundEvent((HttpSessionBindingListener) unbound, name, unbound);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeValue(String name) {
        ensureValidSession();
        this.removeAttribute(name);
    }

    @Override
    public void invalidate() {
        if (!valid) {
            throw new IllegalStateException("this session is already invalid");
        }
        valid = false;

        Map<String, String> unboundsEncoded = execute(new JedisCallBack<Map<String, String>>() {
            @Override
            public Map<String, String> execute(Jedis jedis) {
                Map<String, String> unboundsEncoded = jedis.hgetAll(keyWithPrefix);
                jedis.del(keyWithPrefix);
                return unboundsEncoded;
            }
        });

        for (Map.Entry<String, String> unboundEncoded : unboundsEncoded.entrySet()) {
            String encodedValue = unboundEncoded.getValue();
            Object unboundObject = deserializeObject(encodedValue);
            if (unboundObject != null && unboundObject instanceof HttpSessionBindingListener) {
                String name = unboundEncoded.getKey();
                fireUnboundEvent((HttpSessionBindingListener) unboundObject, name, unboundObject);
            }
        }
    }

    @Override
    public boolean isNew() {
        ensureValidSession();
        return create;
    }

    @Override
    public String toString() {
        Client redisClient = execute(new JedisCallBack<Client>() {
            @Override
            public Client execute(Jedis jedis) {
                return jedis.getClient();
            }
        });
        String host = redisClient.getHost();
        int port = redisClient.getPort();
        return String.format("RedisHttpSession(id: %s) from %s:%d", sessionId, host, port);
    }

    /**
     * 更新当前会话的最后访问时间，通常在本次会话结束前调用。
     */
    protected void renewLastAccessedTimeIfValid() {
        if (!valid) {
            return;
        }

        execute(new JedisCallBack<Void>() {
            @Override
            public Void execute(Jedis jedis) {
                jedis.hset(keyWithPrefix, META_LAST_ACCESSED_TIME, serializeObject(System.currentTimeMillis()));
                return null;
            }
        });
    }

    private void firePassivateEvent(HttpSessionActivationListener listener, String name, Object value) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
        listener.sessionWillPassivate(event);
    }

    private void fireActivateEvent(HttpSessionActivationListener listener, String name, Object value) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
        listener.sessionDidActivate(event);
    }

    private void fireBoundEvent(HttpSessionBindingListener listener, String name, Object value) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
        listener.valueBound(event);
    }

    private void fireUnboundEvent(HttpSessionBindingListener listener, String name, Object value) {
        HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
        listener.valueUnbound(event);
    }

    private Object deserializeObject(String base64) {
        try {
            return redisValueSerDe.deserialize(base64);
        } catch (IOException e) {
            logger.error("failed to deserialize the object from redis", e);
            return null;
        }
    }

    private String serializeObject(Object object) {
        try {
            return redisValueSerDe.serialize(object);
        } catch (IOException e) {
            logger.error("failed to serialize the object to redis", e);
            return null;
        }
    }

    private void ensureValidSession() throws IllegalStateException {
        if (!valid) {
            throw new IllegalStateException("this session is invalid");
        }
    }

    private <T> T execute(JedisCallBack<T> callBack) {
        Jedis jedis = jedisPool.getResource();
        try {
            return callBack.execute(jedis);
        } catch (JedisConnectionException ex) {
            jedisPool.returnBrokenResource(jedis);
            jedis = null;
            throw ex;
        } finally {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    private interface JedisCallBack<T> {

        T execute(Jedis jedis);
    }

    private class JedisHgetCallBack implements JedisCallBack<String> {

        private final String name;

        JedisHgetCallBack(String name) {
            this.name = name;
        }

        @Override
        public String execute(Jedis jedis) {
            return jedis.hget(keyWithPrefix, name);
        }
    }

    private class JedisHgetAndHdelCallback implements JedisCallBack<String> {

        private final String name;

        JedisHgetAndHdelCallback(String name) {
            this.name = name;
        }

        @Override
        public String execute(Jedis jedis) {
            String unboundEncoded = jedis.hget(keyWithPrefix, name);
            jedis.hdel(keyWithPrefix, name);
            return unboundEncoded;
        }
    }
}
