/*
 * Copyright 2014 baidu dpop
 * All right reserved.
 *
 */
package com.baidu.dpop.frame.monitor.jdbc.dialect;

import java.text.SimpleDateFormat;

/**
 * 
 * @author huhailiang <br/>
 * @date: 2014-11-28 15:28:56 <br/>
 * 
 *  Oracle 默认格式化SQL的参数
 */
public class OracleSqlParameterFormatDialect extends DefaultSqlParameterFormatDialect {

    @Override
    public String formatParameterObject(Object sqlParameter) {
        if (sqlParameter instanceof java.sql.Timestamp) {
            return "to_timestamp('" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(sqlParameter)
                    + "', 'yyyy-mm-dd hh24:mi:ss.ff3')";
        } else if (sqlParameter instanceof java.util.Date || sqlParameter instanceof java.sql.Date) {
            return "to_date('" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sqlParameter)
                    + "', 'yyyy-mm-dd hh24:mi:ss')";
        } else {
            return super.formatParameterObject(sqlParameter);
        }
    }

}
