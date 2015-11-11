package com.baidu.dpop.frame.core.filter.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * 用于替换Session实现的HttpServletRequest包装器。
 *
 * @param <SessionT> 会话的实现类型
 * @author jiwenhao
 */
class CustomSessionHttpServletRequest<SessionT extends HttpSession> extends HttpServletRequestWrapper {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final HttpServletRequest wrapped;
    private final HttpServletResponse response;
    private final SessionFactory<SessionT> sessionFactory;

    private SessionT currentSession = null;

    public CustomSessionHttpServletRequest(HttpServletRequest request, HttpServletResponse response,
                                           SessionFactory<SessionT> sessionFactory) {
        super(request);
        if (sessionFactory == null) {
            throw new NullPointerException();
        }
        wrapped = request;
        this.response = response;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (currentSession != null) {
            return currentSession;
        }

        SessionT session = sessionFactory.getSession(wrapped, response, create);
        if (session == null) {
            return null;
        }

        currentSession = session;
        return new InvalidationAwareSessionWrapper(session);
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }

    public void close() {
        if (currentSession == null) {
            return;
        }

        try {
            sessionFactory.releaseSession(currentSession, wrapped, response);
        } catch (Exception ex) {
            logger.error("failed to release session " + currentSession, ex);
        }
    }

    private class InvalidationAwareSessionWrapper extends HttpSessionWrapper<SessionT> {

        public InvalidationAwareSessionWrapper(SessionT original) {
            super(original);
        }

        @Override
        public void invalidate() {
            try {
                super.invalidate();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                sessionFactory.releaseSession(currentSession, wrapped, response);
            } catch (Exception ex) {
                logger.error("failed to release session " + currentSession, ex);
            } finally {
                currentSession = null;
            }
        }
    }
}
