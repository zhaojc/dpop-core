/*
 * Copyright 2014 baidu dpop
 * All right reserved.
 *
 */
package com.baidu.dpop.frame.monitor.jdbc.dialect;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.SQLXML;
import java.text.SimpleDateFormat;

/**
 * 
 * @author huhailiang <br/>
 * @date: 2014-11-28 15:28:56 <br/>
 *        默认格式化SQL的参数，支持H2/mysql
 * 
 */
public class DefaultSqlParameterFormatDialect implements SqlParameterFormatDialect {

    protected static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 
     */
    @Override
    public String formatParameterObject(Object sqlParameter) {
        if (sqlParameter == null) {
            return "NULL";
        }

        if (sqlParameter instanceof String) {
            // todo: 需要处理的嵌入引号的问题，
            // 比如test'sasa'asas
            return "'" + sqlParameter + "'";
        }

        if (sqlParameter instanceof java.util.Date || sqlParameter instanceof java.sql.Date
                || sqlParameter instanceof java.sql.Timestamp || sqlParameter instanceof java.sql.Time) {
            return "'" + new SimpleDateFormat(dateFormat).format(sqlParameter) + "'";
        }

        if (sqlParameter instanceof byte[]) {
            return "'<byte[]>'";
        }

        if (sqlParameter instanceof Array) {
            return "'<Array>'";
        }

        if (sqlParameter instanceof InputStream || sqlParameter instanceof Reader || sqlParameter instanceof Ref
                || sqlParameter instanceof Clob || sqlParameter instanceof Blob || sqlParameter instanceof NClob
                || sqlParameter instanceof SQLXML) {
            return "'<" + sqlParameter.getClass().getName() + ">'";
        }

        // 数字型的
        return sqlParameter.toString();
    }

}
