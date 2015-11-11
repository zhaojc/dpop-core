/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.uniqueid.impl;

import org.junit.Test;

import com.baidu.dpop.frame.core.uniqueid.UniqueElement;
import com.baidu.dpop.frame.core.uniqueid.UniqueidService;

/**
 * @author huhailiang
 * @date 2015年3月11日
 */
public class DpopUniqueidServiceTest {

    @Test
    public void testGenerateId() {
        UniqueidService uniqueidService = new DpopUniqueidService();
        for (int i = 0; i < 10; i++) {
            Long id = uniqueidService.generateId();
            UniqueElement uniqueElement = uniqueidService.explainId(id);
            System.out.println("id : " + id);
            System.out.println("id-binary : " + Long.toBinaryString(id));
            System.out.println(uniqueElement);
        }
    }

}
