package com.baidu.dpop.frame.core.base;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * 服务层基类，已实现对于数据库最基本的增删改查操作。
 * 实现子类需要实现{@link #getDao()}接口，返回对应的数据访问（DAO）对象。
 *
 * @param <T> 业务实体类
 * @param <ID> 实体的主键类型
 */
public abstract class BaseService<T, ID extends Serializable>
        implements GenericMapperService<T, ID> {

    public abstract GenericMapperDao<T, ID> getDao();

    @Override
    public T findById(ID id) {
        return getDao().selectByPrimaryKey(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int deleteById(ID id) {
        return getDao().deleteByPrimaryKey(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int insert(T record) {
        return getDao().insert(record);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int insertSelective(T record) {
        return getDao().insertSelective(record);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int updateById(T record) {
        return getDao().updateByPrimaryKey(record);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public int updateByIdSelective(T record) {
        return getDao().updateByPrimaryKeySelective(record);
    }
}
