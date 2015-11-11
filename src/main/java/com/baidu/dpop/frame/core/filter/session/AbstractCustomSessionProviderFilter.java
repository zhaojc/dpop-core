package com.baidu.dpop.frame.core.filter.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * 抽象的自定义servlet会话过滤器。经过此filter后，HttpSession的实现类皆由SessionFactory的实现提供。
 * 子类只需提供获取{@code SessionFactory}实例的方式即可。
 * 通过调用{@link AbstractCustomSessionProviderFilter#AbstractCustomSessionProviderFilter(SessionFactory)}或
 * {@link #setSessionFactory(SessionFactory)}来注入其实例。
 *
 * @see SpringHostedCustomSessionProviderFilter
 * @see CustomSessionProviderFilter
 *
 * @author jiwenhao
 */
public abstract class AbstractCustomSessionProviderFilter implements Filter {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final NullSessionFactory nullSessionFactory = new NullSessionFactory();
    private volatile SessionFactory sessionFactory = nullSessionFactory;

    protected AbstractCustomSessionProviderFilter() {
    }

    public AbstractCustomSessionProviderFilter(SessionFactory sessionFactory) {
        if (sessionFactory == null) {
            throw new NullPointerException();
        }
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        @SuppressWarnings("unchecked")
        CustomSessionHttpServletRequest customSessionReq = new CustomSessionHttpServletRequest(httpServletRequest,
                                                                                               httpServletResponse,
                                                                                               sessionFactory);
        try {
            chain.doFilter(customSessionReq, response);
        } finally {
            try {
                customSessionReq.close();
            } catch (Exception ex) {
                logger.error("failed to close CustomSessionHttpServletRequest", ex);
            }
        }
    }

    @Override
    public void destroy() {
        try {
            sessionFactory.close();
        } catch (Exception e) {
            logger.error("failed to close sessionFactory", e);
        }
    }

    /**
     * 设置会话工厂，通常用于懒加载时调用。
     *
     * @param sessionFactory 会话工厂实例
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        if (this.sessionFactory != null && this.sessionFactory != nullSessionFactory) {
            throw new IllegalStateException("sessionFactory has already been set");
        }
        if (sessionFactory == null) {
            throw new NullPointerException();
        }
        this.sessionFactory = sessionFactory;
    }

    /**
     * 空会话工厂。用于解决子类配置错误时，在使用HttpSession时抛出对应的{@code IllegalStateException}异常。
     */
    private class NullSessionFactory implements SessionFactory {

        @Override
        public HttpSession getSession(HttpServletRequest request, HttpServletResponse response, boolean create) {
            String errorMessage = String.format("The session factory is not installed properly. "
                                                    + "Please check the configuration of %s.",
                                                AbstractCustomSessionProviderFilter.this.getClass());
            throw new IllegalStateException(errorMessage);
        }

        @Override
        public void releaseSession(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        }

        @Override
        public void close() throws IOException {
        }
    }
}
