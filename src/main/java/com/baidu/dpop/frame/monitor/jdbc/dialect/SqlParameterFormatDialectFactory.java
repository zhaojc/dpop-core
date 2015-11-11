/*
 * Copyright 2014 baidu dpop
 * All right reserved.
 *
 */
package com.baidu.dpop.frame.monitor.jdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author huhailiang <br/>
 * @date: 2014-12-01 15:28:56 <br/>
 *        数据库参数格式化方言获取工厂
 */
public class SqlParameterFormatDialectFactory {

    /**
     * 保存格式化方言的容器
     */
    static Map<String, SqlParameterFormatDialect> formatDialectMap = null;

    static DefaultSqlParameterFormatDialect defaultDialect = new DefaultSqlParameterFormatDialect();

    static {
        formatDialectMap = new ConcurrentHashMap<String, SqlParameterFormatDialect>(10);
        OracleSqlParameterFormatDialect oracleDialect = new OracleSqlParameterFormatDialect();

        /** create lookup Map for specific rdbms formatters Dialect */
        formatDialectMap.put(DriverEnum.Oracle1.getDriverClassName(), oracleDialect);
        formatDialectMap.put(DriverEnum.Oracle2.getDriverClassName(), oracleDialect);

        formatDialectMap.put(DriverEnum.SQLServer.getDriverClassName(), defaultDialect);
        formatDialectMap.put(DriverEnum.MYSQL.getDriverClassName(), defaultDialect);
        formatDialectMap.put(DriverEnum.H2.getDriverClassName(), defaultDialect);
    }

    /**
     * 获取一个数据库参数格式化方言
     * 
     * @return
     */

    public static SqlParameterFormatDialect getDialect(Connection realConnection) {
        String driverName = "";
        try {
            DatabaseMetaData dbm = realConnection.getMetaData();
            driverName = dbm.getDriverName();
        } catch (SQLException s) {
            return defaultDialect;
        }
        return getDialect(driverName);
    }

    /**
     * 获取一个数据参数格式化方言
     * 
     * @return
     */
    private static SqlParameterFormatDialect getDialect(String driverName) {
        SqlParameterFormatDialect formatDialect = formatDialectMap.get(driverName);
        if (null != formatDialect) {
            return formatDialect;
        }
        return defaultDialect;
    }

}
