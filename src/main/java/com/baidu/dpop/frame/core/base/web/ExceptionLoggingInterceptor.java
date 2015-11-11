package com.baidu.dpop.frame.core.base.web;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Controller异常日志统一打印拦截器。除了黑名单中指定的异常之外，所有从Controller方法中抛出的异常，都会使用Log4j打印日志。
 * 其中Logger为以Controller类名命名。
 *
 * @author jiwenhao
 */
public class ExceptionLoggingInterceptor extends HandlerInterceptorAdapter {

    private final Logger logger = Logger.getLogger(this.getClass());

    private Set<Class<? extends Exception>> excludedExceptions = Collections.emptySet();

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (ex == null) {
            return;
        }

        if (excludedExceptions.contains(ex.getClass())) {
            return;
        }
        for (Class<? extends Exception> excludedException : excludedExceptions) {
            if (excludedException.isInstance(ex)) {
                return;
            }
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Logger controllerLogger = Logger.getLogger(handlerMethod.getBeanType());
            controllerLogger.error(String.format("uncaught exception from %s", handlerMethod), ex);
        } else {
            logger.error(String.format("uncaught exception from a handler of unknown type: %s", handler), ex);
        }
    }

    /**
     * 异常黑名单。在黑名单中的异常类都不会被日志记录。默认没有黑名单。
     *
     * @param excludedExceptions 异常黑名单
     */
    public void setExcludedExceptions(Collection<String> excludedExceptions) {
        if (excludedExceptions == null || excludedExceptions.isEmpty()) {
            this.excludedExceptions = Collections.emptySet();
            return;
        }

        this.excludedExceptions = new HashSet<Class<? extends Exception>>(excludedExceptions.size());
        try {
            for (String excludedException : excludedExceptions) {
                this.excludedExceptions.add(Class.forName(excludedException).asSubclass(Exception.class));
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("not a valid class", ex);
        }
    }
}
