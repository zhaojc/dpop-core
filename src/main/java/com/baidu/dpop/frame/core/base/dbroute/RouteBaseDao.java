/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.base.dbroute;

import java.io.Serializable;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.baidu.dpop.frame.core.base.GenericMapperDao;
import com.baidu.dpop.frame.core.dbroute.DBRoute;

/**
 * 分表数据访问类（DAO）基类，{@link GenericMapperDao}的抽象实现类，<br>
 * 已实现对于数据库最基本的增删改查操作。实现子类需要实现{@link #getMapper()}接口，<br>
 * 返回对应的数据映射对象。<br>
 * 
 * @author huhailiang
 * @date 2015年7月21日
 */
public abstract class RouteBaseDao<T, ID extends Serializable, RID extends Serializable> extends SqlSessionDaoSupport
        implements RouteGenericMapperDao<T, ID, RID> {

    /**
     * 获取数据映射对象
     * 
     * @see com.baidu.dpop.ctp.common.base.RouteGenericMapper
     * @return 数据映射对象
     */
    public abstract RouteGenericMapper<T, ID> getMapper();

    /**
     * 获取分表对象
     * 
     * @return 数据映射对象
     */
    public abstract DBRoute getDBRoute(RID routeId);

    /**
     * 根据主键删除数据库中的数据
     * 
     * @param id 主键值
     * @return 实际删除的条数
     */
    public int deleteByPrimaryKey(ID id, RID routerId) {
        DBRoute route = getDBRoute(routerId);
        return getMapper().deleteByPrimaryKey(id, route);
    }

    /**
     * 插入一条新的实体记录
     * 
     * @param record 实体记录
     * @return 实际插入的条数
     */
    public int insert(T record, RID routerId) {
        DBRoute route = getDBRoute(routerId);
        return getMapper().insert(record, route);
    }

    /**
     * 插入一条新的实体记录，如果实体对象中的属性为null，则这个属性对应的字段值将不指定（若数据库字段有默认值，则设置为默认值）<br>
     * 如果主键为自增，则实体对象会在插入后会被设置主键的值。<br>
     * 
     * @param record 实体记录
     * @return 实际插入的条数
     */
    public int insertSelective(T record, RID routerId) {
        DBRoute route = getDBRoute(routerId);
        return getMapper().insertSelective(record, route);
    }

    /**
     * 根据主键查询数据库中的记录。
     * 
     * @param id 主键值
     * @return 主键对应的实体记录。若不存在，则返回null。
     */
    public T selectByPrimaryKey(ID id, RID routerId) {
        DBRoute route = getDBRoute(routerId);
        return getMapper().selectByPrimaryKey(id, route);
    }

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。<br>
     * 若实体对象的属性值为null，则对应数据字段值就不会做更新。<br>
     * 
     * @param record 实体对象
     * @return 实际更新的条数
     */
    public int updateByPrimaryKeySelective(T record, RID routerId) {
        DBRoute route = getDBRoute(routerId);
        return getMapper().updateByPrimaryKeySelective(record, route);
    }

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。
     * 
     * @param record 实体对象
     * @return 实际更新的条数
     */
    public int updateByPrimaryKey(T record, RID routerId) {
        DBRoute route = getDBRoute(routerId);
        return getMapper().updateByPrimaryKey(record, route);
    }

}
