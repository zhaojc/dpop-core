package com.baidu.dpop.frame.core.filter.session;

import java.io.Closeable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 会话工厂类，用于创建会话，释放会话。
 *
 * @param <SessionT> 会话类型（HttpSession子类）
 *
 * @author jiwenhao
 */
public interface SessionFactory<SessionT extends HttpSession> extends Closeable {

    /**
     * 获取一个会话实例。若create为true，则创建一个新的会话；若create为false，则从当前请求中获取关联的会话，在没有的情况下返回null。
     *
     * @param request 请求
     * @param response 响应
     * @param create 是否创建
     * @return 会话实例
     *
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    SessionT getSession(HttpServletRequest request, HttpServletResponse response, boolean create);

    /**
     * 将一个会话从当前请求中释放，并释放与会话关联的资源。
     * 需要注意的是这个方法仅仅到request的生命周期结束后调用，并不是关闭会话的意思。
     *
     * @param request 请求
     * @param response 响应
     * @param session 需要被释放的会话
     */
    void releaseSession(SessionT session, HttpServletRequest request, HttpServletResponse response);
}
