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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.dpop.frame.monitor.executstack.aop.ExecutStackTrace;
import com.baidu.dpop.frame.monitor.executstack.context.ExecutContextUtils;
import com.baidu.dpop.frame.monitor.jdbc.SqlFormat;

/**
 * 
 * @author huhailiang <br/>
 * @date: 2014-11-28 17:35:56 <br/>
 *  Statement的动态代理类，是监控Statement的入口<br/>
 * 此部分可以监控：Statement执行的SQL
 */
public class StatementProxy implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(StatementProxy.class);
    
    protected Statement realStatement;

    /**
     * 监控批量操作时候执行的SQL (see addBatch, clearBatch and executeBatch) //todo: synchronized
     */
    protected List currentBatch = new ArrayList();

    /**
     * 需要被拦截的执行SQL方法名称
     */
    protected static Set<String> interceptorExecuteMethods = new HashSet<String>(3);

    static {
        interceptorExecuteMethods.add("execute");
        interceptorExecuteMethods.add("executeUpdate");
        interceptorExecuteMethods.add("executeQuery");
    }

    private StatementProxy(Statement realStatementt) {
        this.realStatement = realStatementt;
    }

    public static Statement newProxy(Statement realStatementt) {
        StatementProxy proxy = new StatementProxy(realStatementt);
        return (PreparedStatement) Proxy.newProxyInstance(realStatementt.getClass().getClassLoader(),
                new Class[] { java.sql.Statement.class }, proxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = method.invoke(realStatement, args);
            return result;
        } catch (Throwable t) {
            throw t;
        } finally {
            if (ExecutContextUtils.isOpenExecutMoniter()) {
                try {
                    long endTime = System.currentTimeMillis();
                    String methodName = method.getName();
                    if (interceptorExecuteMethods.contains(methodName)) {
                        String executeSql = SqlFormat.format((String) args[0]);
                        ExecutStackTrace executStackTrace = ExecutContextUtils.getExecutStackTrace();
                        executStackTrace.entrySql(executeSql, startTime, endTime);
                        executStackTrace.leave();
                    }
                } catch (Throwable t) {
                    // todo: 监控程序的异常不影响上层的执行
                    LOG.error("StatementProxy finally has error:", t);
                }
            }

        }
    }

}
