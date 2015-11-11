package com.baidu.dpop.frame.core.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 静态资源读取类servlet。
 *
 * <p>
 * 通过使用{@link #setResources}，指定该静态资源servlet所使用的资源实例。
 * 缺省情况下，默认使用{@link EmptyWebResourceRoot}，即不存在任何可用的资源（对应所有的资源获取请求，会返回404）。
 * </p>
 *
 * <p>
 * 可以通过继承{@link WebResourceServlet}，并在默认构造函数或{@link HttpServlet#init}中使用{@link #setResources}指定
 * {@link WebResourceRoot}实例，来自定义资源的获取方式（如从MFS/NFS中获取资源）。
 * 当时用{@link HttpServlet#init}时，需要添加<code>super.init()</code>，用于加载当前实例所依赖的init-param。
 * </p>
 *
 * <p>
 * init-param参数说明：
 *     cacheDeltaSeconds：静态文件缓存时间（单位：秒）
 *     supportByteRangeRequest：是否支持HTTP分段请求，true或false
 *     defaultHeaderFieldParameterCharset：RFC 5987，HTTP头部参数编码字符集（"UTF-8" / "ISO-8859-1" / mime-charset）
 * </p>
 *
 * <p>
 * HTTP请求参数说明：
 *     filename：文件名称。当传递这个参数时，下载文件的名称将使用这个参数的值；没有传递时，会使用资源的默认名称。
 * </p>
 */
public class WebResourceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private int cacheDeltaSeconds = 24 * 60 * 60; // 一天

    private boolean supportByteRangeRequest = true;

    // 取值范围："UTF-8" / "ISO-8859-1" / mime-charset
    private String defaultHeaderFieldParameterCharset = "UTF-8";

    private String defaultHeaderFieldParameterLang = "zh-Hant-CN";

    private transient WebResourceRoot resources = new EmptyWebResourceRoot();

    @Override
    public void init() {
        ServletConfig config = getServletConfig();

        String deltaStr = config.getInitParameter("cacheDeltaSeconds");
        if (deltaStr != null) {
            this.cacheDeltaSeconds = Integer.parseInt(deltaStr);
        }

        String byteRangeRequestStr = config.getInitParameter("supportByteRangeRequest");
        if (byteRangeRequestStr != null) {
            this.supportByteRangeRequest = Boolean.parseBoolean(byteRangeRequestStr);
        }

        String defaultHeaderFieldParameterCharset = config.getInitParameter("defaultHeaderFieldParameterCharset");
        if (defaultHeaderFieldParameterCharset != null) {
            if (Charset.isSupported(defaultHeaderFieldParameterCharset)) {
                this.defaultHeaderFieldParameterCharset = defaultHeaderFieldParameterCharset;
            } else {
                log(String.format("Charset %s is not supported, using %s instead.", defaultHeaderFieldParameterCharset,
                                     this.defaultHeaderFieldParameterCharset));
            }
        }
    }

    /**
     * Process a HEAD request for the specified resource.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @throws java.io.IOException            if an input/output error occurs
     * @throws javax.servlet.ServletException if a servlet-specified error occurs
     */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Serve the requested resource, without the data content
        serveResource(request, response, false);
    }

    /**
     * Process a GET request for the specified resource.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @throws java.io.IOException            if an input/output error occurs
     * @throws javax.servlet.ServletException if a servlet-specified error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Serve the requested resource, including the data content
        serveResource(request, response, true);
    }

    /**
     * Serve the specified resource, optionally including the data content.
     *
     * @param request  The servlet request we are processing
     * @param response The servlet response we are creating
     * @param content  Should the content be included?
     *
     * @throws java.io.IOException            if an input/output error occurs
     * @throws javax.servlet.ServletException if a servlet-specified error occurs
     */
    protected void serveResource(HttpServletRequest request, HttpServletResponse response, boolean content)
            throws IOException, ServletException {
        String path = getPathFromRequest(request);

        WebResource resource = resources.getResource(path);

        // 404
        if (!resource.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
            return;
        }

        // http precondition
        if (!checkIfHeaders(request, response, resource)) {
            return; // 状态码已经设置，直接返回
        }

        String eTag = resource.getETag();
        long lastModified = resource.getLastModified();
        String resourceName = resource.getName();

        // Validation
        // ETag header
        if (eTag != null) {
            response.setHeader("ETag", eTag);
        }
        // Last-Modified header
        if (lastModified > 0) {
            response.setDateHeader("Last-Modified", lastModified);
        }

        // Freshness
        if (cacheDeltaSeconds >= 0) {
            response.setHeader("Cache-Control", String.format("max-age=%d", cacheDeltaSeconds));
        }

        // Content-Type header
        String mimeType = (resource.getMimeType() == null)
                              ? getServletContext().getMimeType(resourceName) : resource.getMimeType();
        if (mimeType != null) {
            response.setContentType(mimeType);
        }

        // Content-Disposition header
        String filenameVal = request.getParameter("filename");
        if (filenameVal != null) { // 如果指定了filename，则添加下载文件名信息
            String filenameParam = encodeHeaderFieldParameter("filename", filenameVal, true);
            String disposition = "attachment;" + filenameParam;
            response.addHeader("Content-Disposition", disposition);
        }

        long totalLen = resource.getContentLength();

        // Accept byte ranges
        response.setHeader("Accept-Ranges", "bytes");

        try {
            List<Range> ranges = parseRanges(request, resource);
            Range mergedRange = mergeRange(ranges);

            long actualContentLen = mergedRange == null ? totalLen : mergedRange.getLength();
            // Content-Length header
            response.setHeader("Content-Length", String.valueOf(actualContentLen));

            if (mergedRange != null) { // partial request headers
                String contentRange = String.format("bytes %d-%d/%d", mergedRange.getStart(),
                                                       mergedRange.getEnd() - 1, totalLen);
                response.addHeader("Content-Range", contentRange);
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            }

            if (!content) {
                return;
            }

            ServletOutputStream ostream = response.getOutputStream();
            copy(resource, ostream, mergedRange);
        } catch (UnsatisfiableRangeException rangeIsNotSatisfiable) {
            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            response.addHeader("Content-Range", "bytes */" + totalLen);
        }
    }

    /**
     * RFC 5987，HTTP头部参数编码实现。
     *
     * <p>
     * ABNF：
     * <pre>
     * parameter     = reg-parameter / ext-parameter
     * reg-parameter = parmname LWSP "=" LWSP value
     * ext-parameter = parmname "*" LWSP "=" LWSP ext-value
     * </pre>
     * </p>
     *
     * <p>
     * 如title*=iso-8859-1'en'%A3%20rates
     * </p>
     *
     * @param paramName HTTP header中参数的名称，对应上述ABNF中的parmname
     * @param paramVal  HTTP header中参数对应的值（未编码前），对应上述ABNF中的value（extMode=false）或ext-value（extMode=true）
     * @param extMode   是否使用扩展模式（在扩展模式下，paramVal会使用servlet中指定的编码方式编码）
     *
     * @return 包含参数名称与值的字符串，如attr=val
     *
     * @see <a href="http://tools.ietf.org/html/rfc5987#section-3.2.1">RFC 5987 Section 3.2.1</a>
     */
    private String encodeHeaderFieldParameter(String paramName, String paramVal, boolean extMode) {
        if (paramVal == null) {
            return paramName + "=";
        }

        String attr = (extMode) ? paramName + "*" : paramName;
        if (!extMode) {
            return attr + "=" + paramVal;
        }

        try {
            String encodedVal = URLEncoder.encode(paramVal, defaultHeaderFieldParameterCharset);
            return String.format("%s=%s\'%s\'%s", attr, defaultHeaderFieldParameterCharset,
                                    defaultHeaderFieldParameterLang, encodedVal);
        } catch (UnsupportedEncodingException e) {
            return paramName + "="; // not expected here
        }
    }

    /**
     * 从请求中获取相对路径，用于获取实际文件。
     * 默认情况下以<code>INFO_PATH</code>为文件路径，可以通过覆盖这个方法来自定义获取文件路径的方式。
     *
     * @param request HTTP请求
     *
     * @return 路径
     */
    protected String getPathFromRequest(HttpServletRequest request) {
        String pathInfo = request.getPathInfo(); // 请求路径 = 不包含servlet之后去除第一个"/"的路径
        if (pathInfo == null) { // 被配置为默认servlet（映射路径 '/'）
            /*
             * Ref: JSR-000154 JavaTM Servlet 2.4  - SRV.11.2
             * A string containing only the ’/’ character indicates the "default" servlet of
             * the application. In this case the servlet path is the request URI minus the context
             * path and the path info is null.
             */
            return request.getServletPath().substring(1); // 去除'/'
        } else {
            return pathInfo.substring(1); // 去除'/'
        }
    }

    /**
     * 将资源写入HTTP输出流中
     *
     * @param resource 资源实例
     * @param out      HTTP body输出流
     * @param range    范围（若为空则全量拷贝）
     *
     * @throws java.io.IOException 当读写异常发生时
     */
    private void copy(WebResource resource, ServletOutputStream out, Range range) throws IOException {
        if (range == null) {
            resource.copyTo(out);
        } else {
            resource.copyTo(out, range.getStart(), range.getEnd());
        }
    }

    private static final Pattern BYTE_RANGE_SPEC = Pattern.compile("(\\d+)-(\\d+)?");
    private static final Pattern SUFFIX_BYTE_RANGE_SPEC = Pattern.compile("-(\\d+)");

    private static final List<Range> ENTIRE_PRESENTATION = Collections.emptyList();

    /**
     * 从HTTP请求中获取所有分段范围
     *
     * @param request 请求
     *
     * @return 所有分段范围，若不是分段请求（或由于其他原因需要全量返回）则返回emptyList。
     */
    private List<Range> parseRanges(HttpServletRequest request, WebResource resource)
            throws UnsatisfiableRangeException {
        if (!supportByteRangeRequest) {
            return ENTIRE_PRESENTATION;
        }

        String rangeHeader = request.getHeader("Range");
        if (rangeHeader == null) {
            return ENTIRE_PRESENTATION;
        }

        // If-Range
        String ifRange = request.getHeader("If-Range");
        if (ifRange != null) {
            try {
                long dateValidator = request.getDateHeader("If-Range");
                // If-Range: HTTP-date
                if (isModifiedSince(dateValidator, resource.getLastModified())) {
                    return ENTIRE_PRESENTATION;
                }
            } catch (IllegalArgumentException e) {
                // If-Range: entity-tag
                if (!doesETagMatch(ifRange, resource.getETag())) {
                    return ENTIRE_PRESENTATION;
                }
            }
        }

        if (!rangeHeader.startsWith("bytes=")) {
            // An origin server MUST ignore a Range header field that contains a
            // range unit it does not understand.
            return ENTIRE_PRESENTATION;
        }

        String rangeData = rangeHeader.substring(6); // bytes=(.*)
        StringTokenizer dotSplitter = new StringTokenizer(rangeData, ",");
        List<Range> ranges = new LinkedList<Range>();
        while (dotSplitter.hasMoreTokens()) {
            String rangeExpr = dotSplitter.nextToken().trim(); // remove OWS
            if (rangeExpr.isEmpty()) {
                continue;
            }

            long contentLen = resource.getContentLength();

            try {
                Matcher byteRangeSpec = BYTE_RANGE_SPEC.matcher(rangeExpr);
                if (byteRangeSpec.matches()) {
                    long start = Long.parseLong(byteRangeSpec.group(1));
                    String optionalLastByte = byteRangeSpec.group(2);
                    long end = (optionalLastByte == null) ? contentLen : Long.parseLong(optionalLastByte) + 1;
                    // if the specified range(s) are invalid or unsatisfiable, the server SHOULD send a 416 (Range Not
                    // Satisfiable) response.
                    ranges.add(new Range(start, end));
                    continue;
                }

                Matcher suffixByteRangeSpec = SUFFIX_BYTE_RANGE_SPEC.matcher(rangeExpr);
                if (suffixByteRangeSpec.matches()) {
                    long lastBytes = Long.parseLong(suffixByteRangeSpec.group(1));
                    long start = contentLen - lastBytes;
                    ranges.add(new Range(start, contentLen));
                }
            } catch (IllegalArgumentException e) { // 范围非法或过大
                throw new UnsatisfiableRangeException();
            }
        }

        return ranges;
    }

    /**
     * 合并多个范围为一个最大范围
     *
     * @param ranges 范围列表
     *
     * @return 一个最小的包含所有的范围
     */
    private Range mergeRange(List<Range> ranges) {
        if (ranges.isEmpty()) {
            return null;
        }

        long start = -1L;
        long end = -1L;
        for (Range range : ranges) {
            if (start == -1L || start > range.getStart()) {
                start = range.getStart();
            }
            if (end == -1L || end < range.getEnd()) {
                end = range.getEnd();
            }
        }
        return new Range(start, end);
    }

    /**
     * HTTP相关前置条件校验。在条件满足时，返回true。
     * 当条件失败时，在response中设置相关的状态码以及其他数据，并返回false。
     * 此时，调用者应当直接返回，而不应该继续处理。
     * 目前只支持HEAD、GET谓词下的
     * <ol>
     * <li>
     * If-None-Match前置条件 http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.26
     * </li>
     * <li>
     * If-Modified-Since前置条件 http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.25
     * </li>
     * </ol>
     * 对于其他HTTP谓词，都会抛出IllegalArgumentException。
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param resource 资源实例
     *
     * @return 是否满足HTTP前置条件
     *
     * @throws java.io.IOException      当文件读写失败或无法处理HTTP相关对象时
     * @throws IllegalArgumentException 如果当前request并非GET或者HEAD
     */
    private boolean checkIfHeaders(HttpServletRequest request, HttpServletResponse response, WebResource resource)
            throws IOException {
        if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod())) {
            throw new IllegalArgumentException("method must be either GET or HEAD");
        }

        boolean ifModifiedSincePresent = request.getHeader("If-Modified-Since") != null;
        boolean ifNoneMatchPresent = request.getHeader("If-None-Match") != null;

        if (!ifModifiedSincePresent && !ifNoneMatchPresent) {
            return true; // 没有校验逻辑
        }

        String eTag = resource.getETag();
        long lastModified = resource.getLastModified();

        String ifNoneMatchHeader = request.getHeader("If-None-Match");
        boolean noneMatch = !doesETagMatch(ifNoneMatchHeader, eTag);

        if (ifNoneMatchPresent && noneMatch) {
            /*
             * "if no entity tags match, then the server MUST NOT return a 304 (Not Modified) response."
             * Reference: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.26
             */
            return true; // 200
        }
        if (ifModifiedSincePresent) { // 存在If-Modified-Since
            boolean modifiedSince = true;
            try {
                long ifModifiedSinceHeader = request.getDateHeader("If-Modified-Since");
                modifiedSince = isModifiedSince(ifModifiedSinceHeader, lastModified);
            } catch (IllegalArgumentException ignored) {
                // ignored (modifiedSince = true)
            }

            if (modifiedSince) {
                return true; // 200
            }
        }

        /*
         * "if the request method was GET or HEAD, the server SHOULD respond
         * with a 304 (Not Modified) response, including the cache- related
         * header fields (particularly ETag) of one of the entities that matched.
         * "
         * Reference: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.26
         */
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        response.setHeader("ETag", eTag);
        response.setDateHeader("Last-Modified", lastModified);
        return false;
    }

    private boolean doesETagMatch(String httpHeaderContainingETags, String resourceETag) {
        if (httpHeaderContainingETags == null) { // 如果客户端没有指定含有ETag的HTTP头
            return false;
        }
        if ("*".equals(httpHeaderContainingETags)) { // 表示匹配任何资源实体
            return true;
        }

        StringTokenizer commaTokenizer = new StringTokenizer(httpHeaderContainingETags, ",");
        while (commaTokenizer.hasMoreTokens()) {
            String currentEntityTag = commaTokenizer.nextToken().trim();
            if (currentEntityTag.equals(resourceETag)) {
                return true;
            }
        }
        return false;
    }

    private boolean isModifiedSince(long httpHeaderIfModifiedSince, long resourceLastModified) {
        return (resourceLastModified >= httpHeaderIfModifiedSince + 1000); // 资源已修改过
    }

    public void setCacheDeltaSeconds(int cacheDeltaSeconds) {
        this.cacheDeltaSeconds = cacheDeltaSeconds;
    }

    public void setSupportByteRangeRequest(boolean supportByteRangeRequest) {
        this.supportByteRangeRequest = supportByteRangeRequest;
    }

    public void setDefaultHeaderFieldParameterCharset(String defaultHeaderFieldParameterCharset) {
        this.defaultHeaderFieldParameterCharset = defaultHeaderFieldParameterCharset;
    }

    public void setDefaultHeaderFieldParameterLang(String defaultHeaderFieldParameterLang) {
        this.defaultHeaderFieldParameterLang = defaultHeaderFieldParameterLang;
    }

    public void setResources(WebResourceRoot resources) {
        this.resources = resources;
    }
}
