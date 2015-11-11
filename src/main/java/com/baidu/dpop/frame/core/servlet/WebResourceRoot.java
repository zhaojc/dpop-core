package com.baidu.dpop.frame.core.servlet;

/**
 * 资源根接口。可以通过路径查找对应的资源。
 */
public interface WebResourceRoot {

    /**
     * 根据传入的路径获取对应的资源。
     *
     * @param path 用于查找资源的路径
     * @return 与路径关联的资源（不能为空）。若资源没有找到，可以返回{@link EmptyWebResource}
     */
    WebResource getResource(String path);
}
