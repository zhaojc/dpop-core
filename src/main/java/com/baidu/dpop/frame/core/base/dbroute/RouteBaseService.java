/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.base.dbroute;

import java.io.Serializable;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 分表服务层基类，已实现对于数据库最基本的增删改查操作。<br>
 * 实现子类需要实现{@link #getDao()}接口，返回对应的数据访问（DAO）对象。<br>
 * @author huhailiang
 * @date 2015年7月21日
 */
public abstract class RouteBaseService<T, ID extends Serializable, RID extends Serializable> implements
        RouteGenericMapperService<T, ID, RID> {

    public abstract RouteGenericMapperDao<T, ID, RID> getDao();

    @Override
    public T findById(ID id, RID routerId) {
        return getDao().selectByPrimaryKey(id, routerId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int deleteById(ID id, RID routerId) {
        return getDao().deleteByPrimaryKey(id, routerId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int insert(T record, RID routerId) {
        return getDao().insert(record, routerId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int insertSelective(T record, RID routerId) {
        return getDao().insertSelective(record, routerId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int updateById(T record, RID routerId) {
        return getDao().updateByPrimaryKey(record, routerId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int updateByIdSelective(T record, RID routerId) {
        return getDao().updateByPrimaryKeySelective(record, routerId);
    }
}
