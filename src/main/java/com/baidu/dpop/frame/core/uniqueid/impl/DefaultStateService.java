/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.uniqueid.impl;

import com.baidu.dpop.frame.core.uniqueid.StateService;

/**
 * @author huhailiang
 * @date 2015年3月11日
 */
public class DefaultStateService implements StateService {

    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.dpop.frame.core.uniqueid.StateService#getState()
     */
    @Override
    public long getState() {
        // TODO Auto-generated method stub
        return Thread.currentThread().getId();
    }

}
