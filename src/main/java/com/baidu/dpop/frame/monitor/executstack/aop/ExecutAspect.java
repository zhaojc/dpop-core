package com.baidu.dpop.frame.monitor.executstack.aop;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;

import com.baidu.dpop.frame.monitor.executstack.context.ExecutContextUtils;

/**
 * 
 * 
 * 
 * 
 *   <bean id="executAspect" class="com.baidu.dpop.frame.monitor.executstack.ExecutAspect" />
 *   <aop:config proxy-target-class="true">
 *   
 *           <aop:aspect id="serviceExecutAspect" ref="executAspect">
 *               <aop:pointcut id="servicePointcut" expression="execution(* com.baidu.dpop.rmp.*.service.impl.*.*(..))" />
 *               <aop:before          pointcut-ref="servicePointcut" method="beforeAdvice" />
 *               <aop:after           pointcut-ref="servicePointcut" method="afterAdvice" />
 *               <aop:after-throwing  pointcut-ref="servicePointcut" method="afterThrowingAdvice" throwing="exception"/>
 *           </aop:aspect>
 *           
 *           <aop:aspect id="daoExecutAspect" ref="executAspect">
 *               <aop:pointcut id="daoPointcut" expression="execution(* com.baidu.dpop.rmp.*.dao.impl.*.*(..))" />
 *               <aop:before          pointcut-ref="daoPointcut" method="beforeAdvice" />
 *               <aop:after           pointcut-ref="daoPointcut" method="afterAdvice" />
 *               <aop:after-throwing  pointcut-ref="daoPointcut" method="afterThrowingAdvice" throwing="exception" />
 *           </aop:aspect>
 *           
 *           <aop:aspect id="controllerExecutAspect" ref="executAspect">
 *               <aop:pointcut id="controllerPointcut" expression="execution(* com.baidu.dpop.rmp.*.web.controller.*.*(..))" />
 *               <aop:before          pointcut-ref="controllerPointcut" method="beforeAdvice" />
 *               <aop:after           pointcut-ref="controllerPointcut" method="afterAdvice" />
 *               <aop:after-throwing  pointcut-ref="controllerPointcut" method="afterThrowingAdvice" throwing="exception"/>
 *           </aop:aspect>
 *           
 *    </aop:config>
 * 
 * @author huhailiang
 *
 */
@Aspect
public class ExecutAspect {

    /**
     * Logger for this class
     */
    private static final Logger LOGGER = Logger.getLogger(ExecutAspect.class);

    /**
     * This is the method which I would like to execute before a selected method execution.
     */
    public void beforeAdvice(JoinPoint joinPoint) {
        print(joinPoint, "beforeAdvice");
        if (ExecutContextUtils.isOpenExecutMoniter()) {
            String methodSignature = joinPoint.getSignature().toString();
            ExecutStackTrace executStackTrace = ExecutContextUtils.getExecutStackTrace();
            executStackTrace.entryJavaMethod(methodSignature, joinPoint.getArgs());
        }

    }

    /**
     * This is the method which I would like to execute after a selected method execution.
     */
    public void afterAdvice(JoinPoint joinPoint) {
        print(joinPoint, "afterAdvice");
        if (ExecutContextUtils.isOpenExecutMoniter()) {
            ExecutStackTrace executStackTrace = ExecutContextUtils.getExecutStackTrace();
            executStackTrace.leave();
        }
    }

    /**
     * This is the method which I would like to execute if there is an exception raised.
     */
    public void afterThrowingAdvice(JoinPoint joinPoint, Throwable exception) {
        print(joinPoint, "afterThrowingAdvice");
        if (ExecutContextUtils.isOpenExecutMoniter()) {
            ExecutStackTrace executStackTrace = ExecutContextUtils.getExecutStackTrace();
            executStackTrace.setLeaveException(exception);
        }
    }

    private void print(JoinPoint joinPoint, String joinPointName) {
    }
}
