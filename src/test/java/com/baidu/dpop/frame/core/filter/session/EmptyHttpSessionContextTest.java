package com.baidu.dpop.frame.core.filter.session;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

public class EmptyHttpSessionContextTest {

    private EmptyHttpSessionContext sut;

    @Before
    public void setUp() {
        sut = new EmptyHttpSessionContext();
    }

    @Test
    public void testGetSession() throws Exception {
        HttpSession session = sut.getSession("foobar");
        assertNull(session);
    }

    @Test
    public void testGetIds() throws Exception {
        Enumeration ids = sut.getIds();
        assertFalse(ids.hasMoreElements());
    }
}
