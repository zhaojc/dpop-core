package com.baidu.dpop.frame.core.base.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import com.baidu.dpop.frame.core.test.SampleBaseTestCase;

public class JsonOnlyJsonResultExceptionNegotiatorTest extends SampleBaseTestCase {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private HttpServletRequest request;

    private JsonOnlyJsonResultExceptionNegotiator sut;

    @Before
    public void setUp() {
        sut = new JsonOnlyJsonResultExceptionNegotiator();
    }

    private void requestAccepts(String acceptHeader) {
        when(request.getHeader(eq("Accept"))).thenReturn(acceptHeader);
    }

    private void exerciseAndAssertAcceptable() {
        assertTrue(sut.isAcceptable(request));
    }

    private void exerciseAndAssertNotAcceptable() {
        assertFalse(sut.isAcceptable(request));
    }

    @Test
    public void testIsAcceptable1() throws Exception {
        requestAccepts(null);
        exerciseAndAssertNotAcceptable();
    }

    @Test
    public void testIsAcceptable2() throws Exception {
        requestAccepts("application/json");
        exerciseAndAssertAcceptable();
    }

    @Test
    public void testIsAcceptable3() throws Exception {
        requestAccepts("application/json;q=0.8,text/html;q=0.1,*/*;q=0.1");
        exerciseAndAssertAcceptable();
    }

    @Test
    public void testIsAcceptable4() throws Exception {
        requestAccepts("application/json;q=0.1,text/html;q=0.8,*/*;q=0.1");
        exerciseAndAssertNotAcceptable();
    }

    @Test
    public void testIsAcceptable5() throws Exception {
        requestAccepts("text/html;q=0.8,*/*;q=0.2");
        exerciseAndAssertNotAcceptable();
    }

    @Test
    public void testIsAcceptable6() throws Exception {
        requestAccepts("text/html");
        exerciseAndAssertNotAcceptable();
    }
}
