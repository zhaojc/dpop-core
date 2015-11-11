package com.baidu.dpop.frame.core.filter.session;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
public class HttpSessionWrapper<SessionT extends HttpSession> implements HttpSession {

    private final SessionT original;

    public HttpSessionWrapper(SessionT original) {
        if (original == null) {
            throw new NullPointerException();
        }
        this.original = original;
    }

    @Override
    public long getCreationTime() {
        return original.getCreationTime();
    }

    @Override
    public String getId() {
        return original.getId();
    }

    @Override
    public long getLastAccessedTime() {
        return original.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return original.getServletContext();
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        original.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return original.getMaxInactiveInterval();
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return original.getSessionContext();
    }

    @Override
    public Object getAttribute(String name) {
        return original.getAttribute(name);
    }

    @Override
    public Object getValue(String name) {
        return original.getValue(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        return original.getAttributeNames();
    }

    @Override
    public String[] getValueNames() {
        return original.getValueNames();
    }

    @Override
    public void setAttribute(String name, Object value) {
        original.setAttribute(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
        original.putValue(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        original.removeAttribute(name);
    }

    @Override
    public void removeValue(String name) {
        original.removeValue(name);
    }

    @Override
    public void invalidate() {
        original.invalidate();
    }

    @Override
    public boolean isNew() {
        return original.isNew();
    }

    public SessionT getSession() {
        return this.original;
    }
}
