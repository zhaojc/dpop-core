package com.baidu.dpop.frame.core.servlet;

/**
 * 空资源根
 */
public class EmptyWebResourceRoot implements WebResourceRoot {

    @Override
    public WebResource getResource(String path) {
        return new EmptyWebResource();
    }
}
