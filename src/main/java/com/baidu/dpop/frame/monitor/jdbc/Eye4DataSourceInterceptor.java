package com.baidu.dpop.frame.monitor.jdbc;

import java.sql.Connection;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.baidu.dpop.frame.monitor.executstack.context.ExecutContextUtils;
import com.baidu.dpop.frame.monitor.jdbc.proxy.ConnectionProxy;

/**
 * @author huhailiang <br/>
 * @date: 2014-11-28 15:27:56 <br/>
 * 
 * 数据源拦截器，<br/>
 * 主要职责是拦截数据源，获取Connection的代理类<br/>
 * 
 * 配置如下：<br/>
 *  <bean id="dataSourceMonitorInterceptor" class="com.baidu.dpop.frame.monitor.jdbc.Eye4DataSourceInterceptor" />  
 *  <bean id="dataSourceMonitorAutoProxyCreator" class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator">  
 *         <property name="interceptorNames">  
 *             <list>  
 *                <value>dataSourceMonitorInterceptor</value>  
 *             </list>  
 *         </property>  
 *        <property name="beanNames">  
 *             <list>  
 *                <value>dataSource</value>  
 *             </list>  
 *         </property>  
 * </bean>
 *  
 */
public class Eye4DataSourceInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (ExecutContextUtils.isOpenExecutMoniter()) {
            if (result instanceof Connection) {
                Connection conn = (Connection) result;
                return ConnectionProxy.newProxy(conn);
            }
        }
        return result;
    }

}
