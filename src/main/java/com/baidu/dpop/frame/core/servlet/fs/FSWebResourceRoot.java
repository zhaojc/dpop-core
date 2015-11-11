package com.baidu.dpop.frame.core.servlet.fs;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.baidu.dpop.frame.core.servlet.EmptyWebResource;
import com.baidu.dpop.frame.core.servlet.WebResource;
import com.baidu.dpop.frame.core.servlet.WebResourceRoot;

/**
 * 文件系统资源根。将文件系统的目录作为web资源提供。
 */
public class FSWebResourceRoot implements WebResourceRoot {

    private final String basePath;

    /**
     * 构造一个文件系统资源根实例
     * @param basePath 对应web根路径的文件系统路径
     */
    public FSWebResourceRoot(String basePath) {
        if (basePath == null) {
            throw new IllegalArgumentException("basePath must not be null");
        }
        this.basePath = basePath;
    }

    @Override
    public WebResource getResource(String path) {
        if (path == null) {
            return new EmptyWebResource();
        }
        File file = new File(FilenameUtils.concat(basePath, path));
        return new FSWebResource(file);
    }
}
