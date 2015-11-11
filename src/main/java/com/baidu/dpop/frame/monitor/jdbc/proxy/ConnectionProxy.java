/*
 * Copyright 2014 baidu dpop
 * All right reserved.
 *
 */

package com.baidu.dpop.frame.monitor.jdbc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.dpop.frame.monitor.executstack.aop.ExecutStackTrace;
import com.baidu.dpop.frame.monitor.executstack.context.ExecutContextUtils;
import com.baidu.dpop.frame.monitor.jdbc.dialect.SqlParameterFormatDialect;
import com.baidu.dpop.frame.monitor.jdbc.dialect.SqlParameterFormatDialectFactory;

/**
 * @author huhailiang <br/>
 * @date: 2014-11-28 17:28:56 <br/>
 * Connection的动态代理类，是监控Connection的入口<br/>
 * 此部分可以监控：
 * （1）事务
 */
public class ConnectionProxy implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionProxy.class);

    protected static Set<String> TRANSACTION_BEGIN_METHODS = new HashSet<String>(2);
    static {
        TRANSACTION_BEGIN_METHODS.add("setAutoCommit");
    }

    protected static Set<String> TRANSACTION_END_METHODS = new HashSet<String>(2);
    static {
        TRANSACTION_END_METHODS.add("commit");
        TRANSACTION_END_METHODS.add("rollback");
    }

    protected Connection realConnection;
    protected SqlParameterFormatDialect formatDialect;

    private ConnectionProxy(Connection realConnection) {
        this.realConnection = realConnection;
        formatDialect = SqlParameterFormatDialectFactory.getDialect(realConnection);
    }

    public static Connection newProxy(Connection realConnection) {
        ConnectionProxy connectionProxy = new ConnectionProxy(realConnection);
        try {
            Object proxy =
                    Proxy.newProxyInstance(realConnection.getClass().getClassLoader(),
                            new Class[] { java.sql.Connection.class }, connectionProxy);
            return (Connection) proxy;
        } catch (Throwable t) {
            LOG.error("Connection newProxy has error:%s", t);
            throw new RuntimeException("invalid or proxy error Connection: ");
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Object result = method.invoke(realConnection, args);
            afterAdvice(proxy, method, args);
            return proxyResult(result, method, args);
        } catch (Throwable t) {
            throw t;
        }
    }

    /**
     * 
     * @param invokeResult
     * @param method
     * @param args
     * @return
     */
    private Object proxyResult(final Object invokeResult, final Method method, final Object[] args) {
        if (!ExecutContextUtils.isOpenExecutMoniter()) {
            return invokeResult;
        }
        if (invokeResult instanceof PreparedStatement) {
            PreparedStatement preparedStatement = (PreparedStatement) invokeResult;
            String preparedSql = (String) args[0];
            return PreparedStatementProxy.newProxy(formatDialect, preparedStatement, preparedSql);
        }
        if (invokeResult instanceof Statement) {
            Statement statement = (Statement) invokeResult;
            return StatementProxy.newProxy(statement);
        }
        return invokeResult;
    }

    private void afterAdvice(final Object proxy, final Method method, final Object[] args) {
        if (!ExecutContextUtils.isOpenExecutMoniter()) {
            return;
        }
        if (isBeginTransaction(method, args)) {
            ExecutStackTrace executStackTrace = ExecutContextUtils.getExecutStackTrace();
            String isolationDes = getTransactionIsolationDes();
            executStackTrace.entryTransaction(String.format("Transaction begin,Isolation:[%s] ", isolationDes));
            return;
        }

        if (isEndTransaction(method, args)) {
            ExecutStackTrace executStackTrace = ExecutContextUtils.getExecutStackTrace();
            executStackTrace.leave();
            return;
        }
    }

    private boolean isBeginTransaction(final Method method, final Object[] args) {
        String methodName = method.getName();
        if (TRANSACTION_BEGIN_METHODS.contains(methodName)) {
            boolean isAutoCommit = false;
            try {
                isAutoCommit = realConnection.getAutoCommit();
            } catch (SQLException e) {
                LOG.error("realConnection getAutoCommit has error:", e);
            }
            // 当前没开启事务，并调用setAutoCommit设置成false
            return !isAutoCommit && !(Boolean) args[0];
        }
        return false;
    }

    private boolean isEndTransaction(final Method method, final Object[] args) {
        return TRANSACTION_END_METHODS.contains(method.getName());
    }

    /**
     * 获取当前连接的事务隔离级别详情
     * 
     * @return
     */
    private String getTransactionIsolationDes() {
        if (realConnection == null) {
            return "TRANSACTION_NONE";
        }
        try {
            int isolation = realConnection.getTransactionIsolation();
            switch (isolation) {
                case Connection.TRANSACTION_NONE:
                    return "TRANSACTION_NONE";
                case Connection.TRANSACTION_READ_UNCOMMITTED:
                    return "TRANSACTION_READ_UNCOMMITTED";
                case Connection.TRANSACTION_READ_COMMITTED:
                    return "TRANSACTION_READ_COMMITTED";
                case Connection.TRANSACTION_REPEATABLE_READ:
                    return "TRANSACTION_REPEATABLE_READ";
                case Connection.TRANSACTION_SERIALIZABLE:
                    return "TRANSACTION_SERIALIZABLE";
                default:
                    return "TRANSACTION_NONE";
            }
        } catch (SQLException e) {
            return "TRANSACTION_NONE";
        }
    }

}
