package com.baidu.dpop.frame.core.filter.session;

import java.lang.reflect.Constructor;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

/**
 * 自定义servlet会话过滤器。经过此filter后，HttpSession的实现类皆由SessionFactory的实现提供。
 *
 * <p>
 * <h3>当使用web.xml初始化时：</h3>
 *
 * 需要添加如下init-param：<br/>
 *     sessionFactory：指定SessionFactory实例的类名。SessionFactory的实例借助此参数构造，
 *                     sessionFactory类需要包含一个带FilterConfig参数的构造函数或一个无参的构造函数（优先级按序）
 * </p>
 * <p>
 * <h3>当使用{@link org.springframework.web.filter.DelegatingFilterProxy}初始化时：</h3>
 * 可以通过{@link CustomSessionProviderFilter#CustomSessionProviderFilter(SessionFactory)}构造。
 * </p>
 *
 * @author jiwenhao
 */
public class CustomSessionProviderFilter extends AbstractCustomSessionProviderFilter {

    private final Logger logger = Logger.getLogger(this.getClass());

    public CustomSessionProviderFilter() {
    }

    public CustomSessionProviderFilter(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String sessionFactoryVal = filterConfig.getInitParameter("sessionFactory");
        if (sessionFactoryVal == null) {
            logger.info("sessionFactory param is not set, and default session factory is used");
            this.setSessionFactory(new DefaultSessionFactory());
            return;
        }

        try {
            SessionFactory sessionFactory;
            Class<?> sessionFactoryClz = Class.forName(sessionFactoryVal);
            // sessionFactory类需要包含一个带FilterConfig参数的构造函数或一个无参的构造函数
            try {
                Constructor<?> constructor = sessionFactoryClz.getConstructor(FilterConfig.class);
                sessionFactory = (SessionFactory) constructor.newInstance(filterConfig);
            } catch (NoSuchMethodException constructorNotFound) {
                try {
                    Constructor<?> constructor = sessionFactoryClz.getConstructor();
                    sessionFactory = (SessionFactory) constructor.newInstance();
                } catch (NoSuchMethodException e) {
                    throw new ServletException(String.format("constructor %s() or %s(FilterConfig) is required",
                                                             sessionFactoryVal, sessionFactoryVal));
                }
            }

            this.setSessionFactory(sessionFactory);
            logger.info("custom session using provider: " + sessionFactoryVal);
        } catch (ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
