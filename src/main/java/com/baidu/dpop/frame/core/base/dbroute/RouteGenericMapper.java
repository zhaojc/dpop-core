/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.core.base.dbroute;

import java.io.Serializable;

import org.apache.ibatis.annotations.Param;

import com.baidu.dpop.frame.core.dbroute.DBRoute;

/**
 * 分表数据映射接口。可以通过继承该接口的方式并加上<br>
 *  {@link com.baidu.dpop.frame.core.mybatis.SqlMapper}注解，<br>
 *  让mybatis完成实现类。<br>
 * 
 * @author huhailiang
 * @date 2015年7月21日
 * @param <T>
 * @param <ID>
 * @param <RID>
 */
public interface RouteGenericMapper<T, ID extends Serializable> {

    /**
     * 根据主键删除数据库中的数据
     * 
     * @param id 主键值
     * @return 实际删除的条数
     */
    int deleteByPrimaryKey(@Param("id") ID id, @Param("route") DBRoute route);

    /**
     * 插入一条新的实体记录
     * 
     * @param record 实体记录
     * @return 实际插入的条数
     */
    int insert(@Param("recordBo") T record, @Param("route") DBRoute route);

    /**
     * 插入一条新的实体记录，如果实体对象中的属性为null，<br>
     * 则这个属性对应的字段值将不指定（若数据库字段有默认值，则设置为默认值） 如果主键为自增，<br>
     * 则实体对象会在插入后会被设置主键的值。<br>
     * 
     * @param record 实体记录
     * @return 实际插入的条数
     */
    int insertSelective(@Param("recordBo") T record, @Param("route") DBRoute route);

    /**
     * 根据主键查询数据库中的记录。
     * 
     * @param id 主键值
     * @return 主键对应的实体记录。若不存在，则返回null。
     */
    T selectByPrimaryKey(@Param("id") ID id, @Param("route") DBRoute route);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。<br>
     * 若实体对象的属性值为null，则对应数据字段值就不会做更新。
     * 
     * @param record 实体对象
     * @return 实际更新的条数
     */
    int updateByPrimaryKeySelective(@Param("recordBo") T record, @Param("route") DBRoute route);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。
     * 
     * @param record 实体对象
     * @return 实际更新的条数
     */
    int updateByPrimaryKey(@Param("recordBo") T record, @Param("route") DBRoute route);

}
