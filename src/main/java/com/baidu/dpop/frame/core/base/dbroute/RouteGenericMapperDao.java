/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.base.dbroute;

import java.io.Serializable;

/**
 * 分表数据访问接口，包含基本的增删改查操作以及分页查询接口。
 * @author huhailiang
 * @date 2015年7月21日
 */
public interface RouteGenericMapperDao<T, ID extends Serializable, RID extends Serializable> {

    /**
     * 根据主键删除数据库中的数据
     * @param id 主键值
     * @param routerId 分库分表路由ID
     * @return 实际删除的条数
     */
    int deleteByPrimaryKey(ID id, RID routerId);

    /**
     * 插入一条新的实体记录
     * @param record 实体记录
     * @param routerId 分库分表路由ID
     * @return 实际插入的条数
     */
    int insert(T record, RID routerId);

    /**
     * 插入一条新的实体记录，如果实体对象中的属性为null，则这个属性对应的字段值将不指定（若数据库字段有默认值，则设置为默认值）
     * @param record 实体记录
     * @param routerId 分库分表路由ID
     * @return 实际插入的条数
     */
    int insertSelective(T record, RID routerId);

    /**
     * 根据主键查询数据库中的记录。
     * @param id 主键值
     * @param routerId 分库分表路由ID
     * @return 主键对应的实体记录。若不存在，则返回null。
     */
    T selectByPrimaryKey(ID id, RID routerId);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。若实体对象的属性值为null，则对应数据字段值就不会做更新。
     * @param record 实体对象
     * @param routerId 分库分表路由ID
     * @return 实际更新的条数
     */
    int updateByPrimaryKeySelective(T record, RID routerId);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。
     * @param record 实体对象
     * @param routerId 分库分表路由ID
     * @return 实际更新的条数
     */
    int updateByPrimaryKey(T record, RID routerId);
}
