package com.baidu.dpop.frame.core.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

public class WebResourceServletTest {

    private static final String FILE_ETAG = "\"etag\"";
    private static final byte[] FILE_DATA = new byte[] {1, 2, 3};
    private static final long PAST = System.currentTimeMillis()- 1000;
    private static final long FUTURE = System.currentTimeMillis() + 1000;

    @Mocked
    private WebResourceRoot resourceRoot;
    @Mocked
    private WebResource resource;
    @Mocked
    private HttpServletRequest request;
    @Mocked
    private HttpServletResponse response;
    @Mocked
    private ServletOutputStream out;
    @Mocked
    private ServletConfig config;
    @Mocked
    private ServletContext context;

    @Before
    public void setUp() throws Exception {
        new NonStrictExpectations() {
            {
                resourceRoot.getResource(anyString);
                result = resource;
            }
        };

        final String eTag = FILE_ETAG;
        new NonStrictExpectations() {
            {
                resource.exists();
                result = true;

                resource.getContentLength();
                result = (long) FILE_DATA.length;

                resource.getETag();
                result = eTag;

                resource.getLastModified();
                result = PAST;

                resource.getName();
                result = "name";
            }
        };

        new NonStrictExpectations() {
            {
                request.getMethod();
                result = "GET";

                request.getPathInfo();
                result = "/path/to/file";

                request.getDateHeader(anyString);
                result = -1L;
            }
        };

        new NonStrictExpectations() {
            {
                response.getOutputStream();
                result = out;
            }
        };

        new NonStrictExpectations() {
            {
                config.getServletContext();
                result = context;
            }
        };
    }

    @Test
    public void testNotFound() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                resourceRoot.getResource(anyString);
                result = new EmptyWebResource();
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.setResources(resourceRoot);

        tested.doGet(request, response);

        new Verifications() {
            {
                response.sendError(404, anyString);
            }
        };
    }

    @Test
    public void testServeFullContent() throws IOException, ServletException {
        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        // verify 200
        new Verifications() {
            {
                response.sendError(anyInt, anyString);
                times = 0;

                response.sendError(anyInt);
                times = 0;

                response.setStatus(anyInt);
                times = 0;
            }
        };
    }

    @Test
    public void testIfNoneMatch1() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                request.getHeader("If-None-Match");
                result = FILE_ETAG;
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
        };
    }

    @Test
    public void testIfNoneMatch2() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                request.getHeader("If-None-Match");
                result = "another-etag";
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(anyInt);
                times = 0;
            }
        };
    }

    @Test
    public void testIfNoneMatch3() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                request.getHeader("If-None-Match");
                result = "*";
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
        };
    }

    @Test
    public void testIfModifiedSince1() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                resource.getLastModified();
                result = FUTURE;

                request.getHeader("If-Modified-Since");
                result = toHttpTimeFormat(PAST);

                request.getDateHeader("If-Modified-Since");
                result = PAST;
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(anyInt);
                times = 0;
            }
        };
    }

    @Test
    public void testIfModifiedSince2() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                resource.getLastModified();
                result = PAST;

                request.getHeader("If-Modified-Since");
                result = toHttpTimeFormat(FUTURE);

                request.getDateHeader("If-Modified-Since");
                result = FUTURE;
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
        };
    }

    /**
     * If-Modified-Since不满足但是If-None-Match不满足，状态码应当为304
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    @Test
    public void testIfModifiedSinceAndIfNoneMatch1() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                resource.getLastModified();
                result = PAST;

                request.getHeader("If-Modified-Since");
                result = toHttpTimeFormat(FUTURE);

                request.getDateHeader("If-Modified-Since");
                result = FUTURE;

                request.getHeader("If-None-Match");
                result = FILE_ETAG;
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
        };
    }

    /**
     * If-Modified-Since不满足但是If-None-Match满足，状态码应当为200
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    @Test
    public void testIfModifiedSinceAndIfNoneMatch2() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                resource.getLastModified();
                result = PAST;

                request.getHeader("If-Modified-Since");
                result = toHttpTimeFormat(FUTURE);

                request.getDateHeader("If-Modified-Since");
                result = FUTURE;

                request.getHeader("If-None-Match");
                result = "another-etag";
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(anyInt);
                times = 0;
            }
        };
    }

    /**
     * If-Modified-Since满足但是If-None-Match不满足，状态码应当为200
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    @Test
    public void testIfModifiedSinceAndIfNoneMatch3() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                resource.getLastModified();
                result = FUTURE;

                request.getHeader("If-Modified-Since");
                result = toHttpTimeFormat(PAST);

                request.getDateHeader("If-Modified-Since");
                result = PAST;

                request.getHeader("If-None-Match");
                result = FILE_ETAG;
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(anyInt);
                times = 0;
            }
        };
    }

    /**
     * If-Modified-Since满足但是If-None-Match满足，状态码应当为200
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    @Test
    public void testIfModifiedSinceAndIfNoneMatch4() throws IOException, ServletException {
        // override
        new NonStrictExpectations() {
            {
                resource.getLastModified();
                result = FUTURE;

                request.getHeader("If-Modified-Since");
                result = toHttpTimeFormat(PAST);

                request.getDateHeader("If-Modified-Since");
                result = PAST;

                request.getHeader("If-None-Match");
                result = FILE_ETAG;
            }
        };

        WebResourceServlet tested = new WebResourceServlet();
        tested.init(config);
        tested.setResources(resourceRoot); // override

        tested.doGet(request, response);

        new Verifications() {
            {
                response.setStatus(anyInt);
                times = 0;
            }
        };
    }

    private String toHttpTimeFormat(long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(FUTURE);
    }
}