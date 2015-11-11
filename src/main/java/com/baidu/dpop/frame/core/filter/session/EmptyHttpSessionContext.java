package com.baidu.dpop.frame.core.filter.session;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * 用于适配废弃接口
 *
 * @see HttpSessionContext
 *
 * @author jiwenhao
 */
@SuppressWarnings("deprecation")
public class EmptyHttpSessionContext implements HttpSessionContext {

    @Override
    public HttpSession getSession(String sessionId) {
        return null;
    }

    @Override
    public Enumeration getIds() {
        return Collections.enumeration(Collections.emptySet());
    }
}
