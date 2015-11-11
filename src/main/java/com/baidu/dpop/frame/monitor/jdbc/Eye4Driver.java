/*
 * Copyright 2014 baidu dpop
 * All right reserved.
 *
 */
package com.baidu.dpop.frame.monitor.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.dpop.frame.monitor.jdbc.dialect.DriverEnum;
import com.baidu.dpop.frame.monitor.jdbc.proxy.ConnectionProxy;

/**
 * 
 * @author huhailiang  <br>
 * 二次封装Driver，在JDBC Driver 层面拦截获取Connection然后产生代理类 <br>
 *         两方面职责：<br>
 *         （1）自定义驱动URL<br>
 *         （2）封装驱动的connect方法，包装Connection<br>
 *         jdbc Driver static proxy , <br>
 *         demo: <br>
 *         jdbc.driverClassName=com.baidu.dpop.frame.monitor.jdbc.Eye4Driver<br>
 *         jdbc.url=jdbc:eye4jdbc:mysql://127.0.0.1:3306/rmp<br>
 * 
 * 
 */

public class Eye4Driver implements Driver {

    private static final Logger LOG = LoggerFactory.getLogger(Eye4Driver.class);

    private static final String DRIVER_START = "jdbc:eye4";
    private Driver lastOriginalDriver;

    // registerDriver
    static {
        /*
         * 扫描工程lib下所有驱动，并注册驱动 。
         * 不同容器的类加载器对lib class装载有区别， 
         * 为了防止容器classLoader漏装载驱动，做如下的驱动类扫描
         */
        List<String> errorInitDrivers = new ArrayList<String>(DriverEnum.values().length);
        for (DriverEnum driverEnum : DriverEnum.values()) {
            String driverClassName = driverEnum.getDriverClassName();
            try {
                Class.forName(driverClassName);
            } catch (Exception e) {
                errorInitDrivers.add(driverClassName);
            }
        }
        if (errorInitDrivers.size() > 0) {
            LOG.error(String.format("Init JDBC driver Errors [%s] ", StringUtils.join(errorInitDrivers, ",")));
        }

        try {
            DriverManager.registerDriver(new Eye4Driver());
        } catch (SQLException e) {
            LOG.error("Can't register Eye4Driver!");
            throw new RuntimeException("Can't register Eye4Driver!");
        }
    }

    /**
     * 获取原生的JDBC Driver
     * 
     * @param url
     * @return
     * @throws SQLException
     */
    private Driver getOriginalDriver(String url) throws SQLException {
        if (url.startsWith(DRIVER_START)) {
            Driver d;
            url = url.substring(DRIVER_START.length());
            Enumeration<Driver> e = DriverManager.getDrivers();
            while (e.hasMoreElements()) {
                d = e.nextElement();
                boolean isAcceptsURL = d.acceptsURL(url);
                LOG.info(String.format("Driver[%s] AcceptsURL Res[%s]", d.getClass().getName(), isAcceptsURL));
                if (isAcceptsURL) {
                    return d;
                }
            }
            LOG.error(String.format("classloader has not scan the Driver class[%s]", url));
        }
        return null;
    }

    public boolean acceptsURL(String url) throws SQLException {
        Driver d = getOriginalDriver(url);
        if (d != null) {
            this.lastOriginalDriver = d;
            return true;
        }
        return false;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Driver originalDriver = getOriginalDriver(url);
        if (originalDriver == null) {
            return null;
        }
        String newUrl = url.substring(DRIVER_START.length());

        this.lastOriginalDriver = originalDriver;

        Connection realConnection = null;
        try {
            realConnection = originalDriver.connect(newUrl, info);
        } catch (Exception e) {
            LOG.error(String.format("Driver[%s] connect fail[%s]", originalDriver.getClass().getName(), newUrl));
            printProperties(info);
            throw new SQLException("invalid or proxy error driver url: " + url, e);
        }

        if (realConnection == null) {
            LOG.error(String.format("Driver[%s] connect fail[%s]", originalDriver.getClass().getName(), newUrl));
            throw new SQLException("invalid or unknown driver url: " + url);
        }

        Connection connectionPy = null;
        try {
            connectionPy = ConnectionProxy.newProxy(realConnection);
        } catch (Exception e) {
            LOG.error("ConnectionProxy.newProxy has error fail", e);
            throw new SQLException("invalid or proxy error driver url: " + url, e);
        }

        if (connectionPy == null) {
            throw new SQLException("invalid or proxy error driver url: " + url);
        }

        return connectionPy;
    }

    /**
     * 打印驱动配置信息，在出现问题的时候以供排查问题使用
     * 
     * @param info
     */
    private void printProperties(Properties info) {
        if (null == info) {
            return;
        }
        for (Object key : info.keySet()) {
            LOG.error("key[%s] ---> value[%s]", key.toString(), info.get(key));
        }
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        Driver originalDriver = getOriginalDriver(url);
        if (originalDriver == null) {
            return new DriverPropertyInfo[0];
        }

        this.lastOriginalDriver = originalDriver;
        return originalDriver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        if (null == lastOriginalDriver) {
            return 0;
        }
        return lastOriginalDriver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        if (null == lastOriginalDriver) {
            return 0;
        }
        return lastOriginalDriver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        if (null == lastOriginalDriver) {
            return false;
        }
        return lastOriginalDriver.jdbcCompliant();
    }

}
