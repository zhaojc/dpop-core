/*
 * Copyright 2014 baidu dpop
 * All right reserved.
 *
 */

package com.baidu.dpop.frame.monitor.jdbc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.dpop.frame.monitor.executstack.aop.ExecutStackTrace;
import com.baidu.dpop.frame.monitor.executstack.context.ExecutContextUtils;
import com.baidu.dpop.frame.monitor.jdbc.SqlFormat;
import com.baidu.dpop.frame.monitor.jdbc.dialect.SqlParameterFormatDialect;

/**
 * 
 * @author huhailiang <br/>
 * @date: 2014-11-28 17:30:56 <br/>
 *        PreparedStatement的动态代理类，是监控PreparedStatement的入口<br/>
 *        此部分可以监控：PreparedStatement预编译的SQL
 */
public class PreparedStatementProxy implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PreparedStatementProxy.class);

    protected PreparedStatement realPreparedStatement;
    protected String preparedSql;
    protected SqlParameterFormatDialect formatDialect;

    /**
     * 需要被拦截的set参数方法名称
     */
    protected static Set<String> interceptorSetMethods = new HashSet<String>(30);

    /**
     * 需要被拦截的执行SQL方法名称
     */
    protected static Set<String> interceptorExecuteMethods = new HashSet<String>(10);

    static {
        interceptorSetMethods.add("setBoolean");
        interceptorSetMethods.add("setByte");
        interceptorSetMethods.add("setShort");
        interceptorSetMethods.add("setInt");
        interceptorSetMethods.add("setLong");
        interceptorSetMethods.add("setFloat");
        interceptorSetMethods.add("setDouble");
        interceptorSetMethods.add("setBigDecimal");
        interceptorSetMethods.add("setString");
        interceptorSetMethods.add("setBytes");
        interceptorSetMethods.add("setDate");
        interceptorSetMethods.add("setTime");
        interceptorSetMethods.add("setTimestamp");
        interceptorSetMethods.add("setAsciiStream");
        interceptorSetMethods.add("setUnicodeStream");
        interceptorSetMethods.add("setBinaryStream");
        interceptorSetMethods.add("setObject");
        interceptorSetMethods.add("setCharacterStream");
        interceptorSetMethods.add("setRef");
        interceptorSetMethods.add("setBlob");
        interceptorSetMethods.add("setClob");
        interceptorSetMethods.add("setArray");
        interceptorSetMethods.add("setURL");
        interceptorSetMethods.add("setNString");
        interceptorSetMethods.add("setNCharacterStream");
        interceptorSetMethods.add("setNClob");
        interceptorSetMethods.add("setSQLXML");
        interceptorSetMethods.add("setNClob");

        interceptorExecuteMethods.add("execute");
        interceptorExecuteMethods.add("executeUpdate");
        interceptorExecuteMethods.add("executeQuery");
        interceptorExecuteMethods.add("executeBatch");
    }
    /**
     * setXXX设置参数的时候调用
     */
    protected final List<Object> sqlParamTraceSets = new ArrayList<Object>(30);

    /**
     * 
     * @param i
     * @param sqlParam
     */
    private void setSqlParamTrace(int i, Object sqlParam) {
        String sqlParamStr = formatDialect.formatParameterObject(sqlParam);
        i--; // ArrayList下标开始是0,而PreparedStatement开始是1
        synchronized (sqlParamTraceSets) {
            // 如果设置的下标大于当前sqlParamTraceSets的数量，则前面的参数全部补NULL
            while (i >= sqlParamTraceSets.size()) {
                sqlParamTraceSets.add(sqlParamTraceSets.size(), null);
            }
            sqlParamTraceSets.set(i, sqlParamStr);
        }
    }

    protected String dumpPreparedSql() {
        StringBuffer dumpSql = new StringBuffer();
        int lastPos = 0;
        int qpos = preparedSql.indexOf('?', lastPos); // find position of first question mark
        int argIdx = 0;
        String arg;

        while (qpos != -1) {
            // get stored argument
            synchronized (sqlParamTraceSets) {
                try {
                    arg = (String) sqlParamTraceSets.get(argIdx);
                } catch (IndexOutOfBoundsException e) {
                    arg = "?";
                }
            }
            if (arg == null) {
                arg = "?";
            }

            argIdx++;

            dumpSql.append(preparedSql.substring(lastPos, qpos)); // dump segment of sql up to question mark.
            lastPos = qpos + 1;
            qpos = preparedSql.indexOf('?', lastPos);
            dumpSql.append(arg);
        }
        if (lastPos < preparedSql.length()) {
            dumpSql.append(preparedSql.substring(lastPos, preparedSql.length())); // dump last segment
        }
        return dumpSql.toString();
    }

    private PreparedStatementProxy(SqlParameterFormatDialect formatDialect, PreparedStatement realPreparedStatement,
            String preparedSql) {
        this.realPreparedStatement = realPreparedStatement;
        this.preparedSql = preparedSql;
        this.formatDialect = formatDialect;
    }

    public static PreparedStatement newProxy(SqlParameterFormatDialect formatDialect,
            PreparedStatement realPreparedStatement, String preparedSql) {
        PreparedStatementProxy proxy = new PreparedStatementProxy(formatDialect, realPreparedStatement, preparedSql);
        return (PreparedStatement) Proxy.newProxyInstance(realPreparedStatement.getClass().getClassLoader(),
                new Class[] { java.sql.PreparedStatement.class }, proxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = method.invoke(realPreparedStatement, args);
            return result;
        } catch (Throwable t) {
            throw t;
        } finally {
            long endTime = System.currentTimeMillis();
            String methodName = method.getName();
            if (ExecutContextUtils.isOpenExecutMoniter()) {
                try {
                    // 1、设置参数
                    if (interceptorSetMethods.contains(methodName)) {
                        Integer parameterIndex = (Integer) args[0];
                        Object sqlParam = args[1];
                        setSqlParamTrace(parameterIndex, sqlParam);
                    }

                    // 2、执行
                    if (interceptorExecuteMethods.contains(methodName)) {
                        String executeSql = "";
                        if (null == args || args.length == 0) {
                            String dumpedSql = dumpPreparedSql();
                            executeSql = SqlFormat.format(dumpedSql);
                        } else {
                            executeSql = SqlFormat.format((String) args[0]);
                        }
                        ExecutStackTrace executStackTrace = ExecutContextUtils.getExecutStackTrace();
                        executStackTrace.entrySql(executeSql, startTime, endTime);
                        executStackTrace.leave();
                    }
                } catch (Throwable t) {
                    // todo : 监控程序的异常不影响上层的执行
                    LOG.error("PreparedStatementProxy finally has error:", t);
                }
            }
        }

    }

}
