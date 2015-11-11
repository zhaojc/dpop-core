/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.uniqueid.impl;

import org.junit.Test;

import com.baidu.dpop.frame.core.uniqueid.impl.MacAddressHepler;

/**
 * @author huhailiang
 * @date 2015年3月11日
 */
public class MacAddressHeplerTest {

    @Test
    public void testGetMacAddress() {
        try {
            String macStr = MacAddressHepler.getMacAddress();
            System.out.println("mac:" + macStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetMacExtId() {
        try {
            int macExtId = MacAddressHepler.getMacExtId();
            System.out.println("macExtId:" + macExtId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
