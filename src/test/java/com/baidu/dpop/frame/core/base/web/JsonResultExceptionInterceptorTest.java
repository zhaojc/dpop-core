package com.baidu.dpop.frame.core.base.web;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import com.baidu.dpop.frame.core.test.SampleBaseTestCase;

public class JsonResultExceptionInterceptorTest extends SampleBaseTestCase {

    @Mock
    private JsonResultExceptionNegotiator negotiator;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private HttpServletRequest request;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private HttpServletResponse response;
    @Mock
    private Object handler;

    private Exception exception;

    private JsonResultExceptionInterceptor sut;

    @Before
    public void setUp() {
        sut = new JsonResultExceptionInterceptor();
        sut.setNegotiator(negotiator);

        exception = new Exception();
    }

    private void exercise() throws Exception {
        sut.afterCompletion(request, response, handler, exception);
    }

    private void verifyResponseIsNotUsed() throws IOException {
        verify(response, never()).getWriter();
        verify(response, never()).getOutputStream();
    }

    @Test
    public void testInterceptIfNoException() throws Exception {
        sut.afterCompletion(request, response, handler, null);
        verifyResponseIsNotUsed();
    }

    @Test
    public void testInterceptIfResponseIsCommitted() throws Exception {
        when(response.isCommitted()).thenReturn(true);
        exercise();
        verifyResponseIsNotUsed();
    }

    @Test
    public void testInterceptIfRequestIsNotAcceptable() throws Exception {
        when(response.isCommitted()).thenReturn(false);
        when(negotiator.isAcceptable(any(HttpServletRequest.class))).thenReturn(false);
        exercise();
        verifyResponseIsNotUsed();
    }

    @Test
    public void testInterceptIfRequestIsAcceptable() throws Exception {
        when(response.isCommitted()).thenReturn(false);
        when(negotiator.isAcceptable(any(HttpServletRequest.class))).thenReturn(true);
        exercise();
        verify(response).getWriter();
    }

    @Test
    public void testInterceptIfRequestIsAcceptableAndWriterIsNA() throws Exception {
        when(response.isCommitted()).thenReturn(false);
        when(response.getWriter()).thenThrow(new IllegalStateException());
        when(negotiator.isAcceptable(any(HttpServletRequest.class))).thenReturn(true);
        exercise();
        verify(response).getOutputStream();
    }
}
