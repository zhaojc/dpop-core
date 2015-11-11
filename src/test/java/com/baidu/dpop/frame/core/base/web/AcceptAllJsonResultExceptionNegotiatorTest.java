package com.baidu.dpop.frame.core.base.web;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import com.baidu.dpop.frame.core.test.SampleBaseTestCase;

public class AcceptAllJsonResultExceptionNegotiatorTest extends SampleBaseTestCase {

    @Mock(answer = Answers.RETURNS_MOCKS)
    private HttpServletRequest request;

    @Test
    public void testIsAlwaysAcceptable() throws Exception {
        Assert.assertTrue(new AcceptAllJsonResultExceptionNegotiator().isAcceptable(request));
    }
}