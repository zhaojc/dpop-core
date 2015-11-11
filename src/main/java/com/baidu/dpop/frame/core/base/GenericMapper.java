package com.baidu.dpop.frame.core.base;

import java.io.Serializable;

/**
 * 数据映射接口。可以通过继承该接口的方式并加上
 * {@link com.baidu.dpop.frame.core.mybatis.SqlMapper}注解，让mybatis完成实现类。
 * 创建日期：2014-6-26
 *
 * @param <T> 业务实体类
 * @param <ID> 实体的主键类型
 * @author huhailiang
 */
public interface GenericMapper<T, ID extends Serializable> {

    /**
     * 根据主键删除数据库中的数据
     * @param id 主键值
     * @return 实际删除的条数
     */
    int deleteByPrimaryKey(ID id);

    /**
     * 插入一条新的实体记录
     * @param record 实体记录
     * @return 实际插入的条数
     */
    int insert(T record);

    /**
     * 插入一条新的实体记录，如果实体对象中的属性为null，则这个属性对应的字段值将不指定（若数据库字段有默认值，则设置为默认值）
     * 如果主键为自增，则实体对象会在插入后会被设置主键的值。
     * @param record 实体记录
     * @return 实际插入的条数
     */
    int insertSelective(T record);

    /**
     * 根据主键查询数据库中的记录。
     * @param id 主键值
     * @return 主键对应的实体记录。若不存在，则返回null。
     */
    T selectByPrimaryKey(ID id);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。若实体对象的属性值为null，则对应数据字段值就不会做更新。
     * @param record 实体对象
     * @return 实际更新的条数
     */
    int updateByPrimaryKeySelective(T record);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。
     * @param record 实体对象
     * @return 实际更新的条数
     */
    int updateByPrimaryKey(T record);
}
