package com.baidu.dpop.frame.core.filter.session;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.baidu.dpop.frame.core.test.SampleBaseTestCase;

public class DefaultSessionFactoryTest extends SampleBaseTestCase {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private DefaultSessionFactory sut;

    @Before
    public void setUp() {
        sut = new DefaultSessionFactory();
    }

    @Test
    public void testGetSession1() throws Exception {
        sut.getSession(request, response, true);
        verify(request).getSession(eq(true));
    }

    @Test
    public void testGetSession2() throws Exception {
        sut.getSession(request, response, false);
        verify(request).getSession(eq(false));
    }
}
