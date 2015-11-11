package com.baidu.dpop.frame.core.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface WebResource {

    /**
     * See {@link java.io.File#lastModified()}.
     */
    long getLastModified();

    /**
     * See {@link java.io.File#exists()}.
     */
    boolean exists();

    /**
     * See {@link java.io.File#getName()}.
     */
    String getName();

    /**
     * See {@link java.io.File#length()}.
     */
    long getContentLength();

    /**
     * 获取该资源的ETag标识。
     *
     * @return 资源的ETag标识，不能为null。（如果是强标志的话，不要遗漏ETag两侧的双引号）
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.19">RFC2616 Section 14.19</a>
     */
    String getETag();

    /**
     * 获取资源的MIME类型。
     *
     * @return 资源的MIME类型，若MIME类型未知，返回null。
     */
    String getMimeType();

    /**
     * 将资源全部输出到输出流中。
     *
     * @param out 输出流
     *
     * @throws java.io.IOException
     */
    void copyTo(OutputStream out) throws IOException;

    /**
     * 按指定范围，将资源输出到输出流中。
     *
     * @param out 输出流
     * @param start 起始下标（包括）
     * @param end 结束下标（不包括）
     *
     * @throws java.io.IOException
     */
    void copyTo(OutputStream out, long start, long end) throws IOException;
}
