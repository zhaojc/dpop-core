/*
 * Copyright 2014 baidu dpop
 * All right reserved.
 *
 */

package com.baidu.dpop.frame.monitor.jdbc.dialect;

/**
 * 
 * @author huhailiang <br/>
 * @date: 2014-11-28 15:28:55 <br/>
 *        格式化SQL的参数: 目前支持： Mysql/H2/Oracle
 * 
 *        SQL Parameter format interface.Support DB：Mysql/H2/Oracle
 * 
 * 
 */
public interface SqlParameterFormatDialect {

    /**
     * 格式化SQL执行的参数，目前不同的数据库对日期处理方式不同
     * 
     * @param sqlParameter
     * @return
     */
    String formatParameterObject(Object sqlParameter);
}
