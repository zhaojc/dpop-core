package com.baidu.dpop.frame.core.servlet;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 空资源实例。
 */
public class EmptyWebResource implements WebResource {

    @Override
    public long getLastModified() {
        return -1;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public long getContentLength() {
        return 0;
    }

    @Override
    public String getETag() {
        return null;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public void copyTo(OutputStream out) throws IOException {
    }

    @Override
    public void copyTo(OutputStream out, long start, long end) throws IOException {
    }
}
