package com.baidu.dpop.frame.core.base.web;

/**
 * 前端类异常。
 *
 * 通过抛出这个异常，可以传递给前端具体的错误提示信息。
 */
public class FrontendException extends RuntimeException {

    /**
     * 构造一个前端类的异常，传入错误提示信息。
     * @param message 错误提示信息
     */
    public FrontendException(String message) {
        super(message);
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage(); // TODO
    }
}
