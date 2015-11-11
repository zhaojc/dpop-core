package com.baidu.dpop.frame.core.filter.session;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 默认的会话工厂实现。提供原有Servlet容器的Session实现。
 *
 * @author jiwenhao
 */
public class DefaultSessionFactory implements SessionFactory<HttpSession> {

    @Override
    public HttpSession getSession(HttpServletRequest request, HttpServletResponse response, boolean create) {
        return request.getSession(create);
    }

    @Override
    public void releaseSession(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
    }

    @Override
    public void close() throws IOException {
    }
}
