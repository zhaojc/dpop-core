/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.uniqueid;

/**
 * @author huhailiang
 * @date 2015年3月11日
 * 
 * 
 *       处理状态获取接口<br>
 *       一般可以取值: 处理线程ID(ThreadId)、用户ID(UserId)、业务流程ID等(UnitId)<br>
 *       默认是处理线程ID<br>
 *       取值会压缩至区间在0-16<br>
 */
public interface StateService {

    /**
     * 获取生成ID时，处理的状态<br>
     * 一般可以取值<br>
     * : 处理线程ID(ThreadId)、用户ID(UserId)、业务流程ID等(UnitId)<br>
     * 默认是处理线程ID<br>
     * 
     * @return
     */
    public long getState();
}
