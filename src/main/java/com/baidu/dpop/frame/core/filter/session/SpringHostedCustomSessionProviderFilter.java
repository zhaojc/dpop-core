package com.baidu.dpop.frame.core.filter.session;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 通过Spring的容器中获取{@link SessionFactory}实例，来构造自定义servlet会话过滤器。
 * 使用者只需在Spring容器中添加{@link SessionFactory}的实现。
 *
 * 如果这个bean在当前filter初始化之前不存在，例如不是通过{@link org.springframework.web.context.ContextLoaderListener}
 * 事先初始化Spring容器，而是使用{@link org.springframework.web.servlet.DispatcherServlet}，
 * 则需要添加<code>lazyLoad = true</code>的init-param，在第一次请求的时候获取。默认lazyLoad为false，即在<code>init()</code>时
 * 获取。
 *
 * @author jiwenhao
 */
public class SpringHostedCustomSessionProviderFilter extends AbstractCustomSessionProviderFilter {

    private final Logger logger = Logger.getLogger(this.getClass());

    private boolean lazyLoad = false;

    private ServletContext servletContext;
    private final AtomicBoolean sessionFactoryLoaded = new AtomicBoolean(false); // 在lazyLoad=true的情况下使用

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.lazyLoad = Boolean.parseBoolean(filterConfig.getInitParameter("lazyLoad"));
        logger.info(String.format("using spring-hosted mode (lazyLoad: %s)", lazyLoad));

        try {
            this.servletContext = filterConfig.getServletContext();
            if (!lazyLoad) {
                this.setSessionFactory(findSessionFactory(servletContext));
            }
        } catch (Exception ex) {
            throw new ServletException("failed to initialize the SpringHostedCustomSessionProviderFilter", ex);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (lazyLoad && !sessionFactoryLoaded.get() && sessionFactoryLoaded.compareAndSet(false, true)) {
            this.setSessionFactory(findSessionFactory(servletContext));
        }
        super.doFilter(request, response, chain);
    }

    private SessionFactory findSessionFactory(ServletContext servletContext) throws ServletException {
        try {
            WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            SessionFactory sessionFactory = springContext.getBean(SessionFactory.class);
            logger.info("SessionFactory: " + sessionFactory);
            return sessionFactory;
        } catch (NoSuchBeanDefinitionException noSuchBean) {
            throw new ServletException("SessionFactory bean is not found", noSuchBean);
        } catch (Exception ex) {
            throw new ServletException("Failed to find SessionFactory bean due to the previous exception", ex);
        }
    }
}
