/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.base.dbroute;

import java.io.Serializable;

/**
 * 分表服务层接口，包含增删改查操作。
 * 
 * @author huhailiang
 * @date 2015年7月21日
 */
public interface RouteGenericMapperService<T, ID extends Serializable, RID extends Serializable> {

    /**
     * 根据主键查询数据库中的记录。
     * 
     * @param id 主键值
     * @param routerId 分库分表路由ID
     * @return 主键对应的实体记录。若不存在，则返回null。
     */
    public T findById(ID id, RID routerId);

    /**
     * 根据主键删除数据库中的数据
     * 
     * @param id 主键值
     * @param routerId 分库分表路由ID
     * @return 实际删除的条数
     */
    public int deleteById(ID id, RID routerId);

    /**
     * 插入一条新的实体记录
     * 
     * @param record 实体记录
     * @param routerId 分库分表路由ID
     * @return 实际插入的条数
     */
    public int insert(T record, RID routerId);

    /**
     * 插入一条新的实体记录（如字段为空，则使用默认值） 如果主键为自增，则实体对象会在插入后会被设置主键的值。
     * 
     * @param record 实体记录
     * @param routerId 分库分表路由ID
     * @return 实际插入的条数
     */
    public int insertSelective(T record, RID routerId);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。
     * 
     * @param record 实体对象
     * @param routerId 分库分表路由ID
     * @return 实际更新的条数
     */
    public int updateById(T record, RID routerId);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。（如字段为空，则不更新）
     * 
     * @param record 实体对象
     * @param routerId 分库分表路由ID
     * @return 实际更新的条数
     */
    public int updateByIdSelective(T record, RID routerId);

}
