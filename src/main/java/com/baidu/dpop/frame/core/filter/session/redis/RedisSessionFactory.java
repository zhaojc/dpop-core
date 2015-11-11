package com.baidu.dpop.frame.core.filter.session.redis;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.baidu.dpop.frame.core.filter.session.SessionFactory;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * 基于Redis的会话工厂
 *
 * @author jiwenhao
 */
public class RedisSessionFactory implements SessionFactory<RedisHttpSession> {

    protected static final String DEFAULT_SESSION_COOKIE_NAME = "REDIS_SID";
    protected static final int DEFAULT_INTERVAL = 60 * 60; // 1 hr

    private final Logger logger = Logger.getLogger(this.getClass());

    private final JedisPool jedisPool;

    private String sessionIdCookieName = DEFAULT_SESSION_COOKIE_NAME;
    private String cookieDomain;
    private int interval = DEFAULT_INTERVAL;
    private int cookieMaxAge = DEFAULT_INTERVAL;

    private String keyPrefix = "";

    /**
     * 构造一个Redis会话工厂实例。构造依赖于FilterConfig，包含如下参数：
     * sessionIdCookieName：会话依赖的cookie名称（默认为{@link RedisSessionFactory#DEFAULT_SESSION_COOKIE_NAME}）
     * interval：会话保存在Redis中的有效时长（单位为秒，若为负，则不会失效）（默认{@link RedisSessionFactory#DEFAULT_INTERVAL}）
     * cookieDomain：cookie的domain域（默认为源服务器域）
     * cookieMaxAge：cookie的有效时长（默认为interval）
     * host：Redis的主机名（必选）
     * port：Redis的端口（必选）
     * keyPrefix：Redis键的前缀（默认没有前缀）
     * maxActive：Redis客户端连接池最大连接数
     * minIdle：Redis客户端连接池最大闲置连接数
     * testOnBorrow：从客户端连接池中获取连接前是否测试可用
     * testOnReturn：给客户端连接池返还连接前是否测试可用
     *
     * @param filterConfig filter配置，详见上述文档
     */
    public RedisSessionFactory(FilterConfig filterConfig) {
        // sessionIdCookieName
        String cookieName = filterConfig.getInitParameter("sessionIdCookieName");
        if (cookieName == null) {
            this.sessionIdCookieName = DEFAULT_SESSION_COOKIE_NAME;
        } else {
            this.sessionIdCookieName = cookieName;
        }

        // interval
        String intervalVal = filterConfig.getInitParameter("interval");
        if (intervalVal == null) {
            this.interval = DEFAULT_INTERVAL;
        } else {
            this.interval = Integer.parseInt(intervalVal);
        }

        // cookie domain
        this.cookieDomain = filterConfig.getInitParameter("cookieDomain");

        // cookie Max-Age
        String cookieMaxAgeVal = filterConfig.getInitParameter("cookieMaxAge");
        if (cookieMaxAgeVal == null) {
            this.cookieMaxAge = this.interval;
        } else {
            this.cookieMaxAge = Integer.parseInt(cookieMaxAgeVal);
        }

        // host
        String host = filterConfig.getInitParameter("host");
        if (host == null) {
            throw new IllegalArgumentException("host is required");
        }

        // port
        String portVal = filterConfig.getInitParameter("port");
        if (portVal == null) {
            throw new IllegalArgumentException("port is required");
        }
        int port = Integer.parseInt(portVal);

        // key prefix
        String keyPrefix = filterConfig.getInitParameter("keyPrefix");
        if (keyPrefix != null) {
            this.keyPrefix = keyPrefix;
        }

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        // maxTotal
        String maxTotalVal = filterConfig.getInitParameter("maxActive");
        if (maxTotalVal != null) {
            jedisPoolConfig.setMaxActive(Integer.parseInt(maxTotalVal));
        }

        // minIdle
        String minIdleVal = filterConfig.getInitParameter("minIdle");
        if (minIdleVal != null) {
            jedisPoolConfig.setMinIdle(Integer.parseInt(minIdleVal));
        }

        // testOnBorrow
        String testOnBorrowVal = filterConfig.getInitParameter("testOnBorrow");
        if (testOnBorrowVal != null) {
            jedisPoolConfig.setTestOnBorrow(Boolean.parseBoolean(testOnBorrowVal));
        }

        // testOnReturn
        String testOnReturnVal = filterConfig.getInitParameter("testOnReturn");
        if (testOnReturnVal != null) {
            jedisPoolConfig.setTestOnReturn(Boolean.parseBoolean(testOnReturnVal));
        }

        this.jedisPool = new JedisPool(jedisPoolConfig, host, port);
    }

    /**
     * 通过JedisPoolConfig配置构造会话工厂。
     *
     * @param jedisPoolConfig jedis配置对象
     * @param host redis主机名
     * @param port redis端口号
     */
    public RedisSessionFactory(JedisPoolConfig jedisPoolConfig, String host, int port) {
        this.jedisPool = new JedisPool(jedisPoolConfig, host, port);
    }

    public void setSessionIdCookieName(String sessionIdCookieName) {
        this.sessionIdCookieName = sessionIdCookieName;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    @Override
    public RedisHttpSession getSession(HttpServletRequest request, HttpServletResponse response, boolean create) {
        String sessionId = getSessionId(request);

        if (!create && sessionId == null) {
            return null;
        }

        // 创建一个新的，或在request中关联一个已存在的会话。
        try {
            RedisHttpSession session;
            if (sessionId == null) { // 新的会话
                session = new RedisHttpSession(jedisPool, interval, keyPrefix, request, response);
            } else { // 关联已存在的会话
                session = new RedisHttpSession(sessionId, jedisPool, interval, keyPrefix, request, response);
            }
            setCookie(sessionIdCookieName, session.getId(), response);
            return session;
        } catch (JedisConnectionException e) {
            logger.error("failed to getSession from redis", e);
            return null;
        }
    }

    @Override
    public void releaseSession(RedisHttpSession session, HttpServletRequest request, HttpServletResponse response) {
        session.renewLastAccessedTimeIfValid();
    }

    @Override
    public void close() throws IOException {
        jedisPool.destroy();
    }

    private String getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            String cookieName = cookie.getName();
            if (sessionIdCookieName.equals(cookieName)) {
                return cookie.getValue(); // 仅支持单一会话
            }
        }
        return null;
    }

    private void setCookie(String name, String value, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        if (cookieDomain != null) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setMaxAge(cookieMaxAge);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
