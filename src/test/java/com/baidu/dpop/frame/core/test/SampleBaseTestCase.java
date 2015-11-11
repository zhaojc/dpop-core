package com.baidu.dpop.frame.core.test;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

public abstract class SampleBaseTestCase {

    @Before
    public final void setUpFixture() {
        MockitoAnnotations.initMocks(this);
    }
}
