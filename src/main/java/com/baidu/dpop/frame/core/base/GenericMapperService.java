package com.baidu.dpop.frame.core.base;

import java.io.Serializable;

/**
 * 服务层接口，包含增删改查操作。
 *
 * @param <T> 业务实体类
 * @param <ID> 实体的主键类型
 */
public interface GenericMapperService<T, ID extends Serializable> {

    /**
     * 根据主键查询数据库中的记录。
     * @param id 主键值
     * @return 主键对应的实体记录。若不存在，则返回null。
     */
    public T findById(ID id);

    /**
     * 根据主键删除数据库中的数据
     * @param id 主键值
     * @return 实际删除的条数
     */
    public int deleteById(ID id);

    /**
     * 插入一条新的实体记录
     * @param record 实体记录
     * @return 实际插入的条数
     */
    public int insert(T record);

    /**
     * 插入一条新的实体记录（如字段为空，则使用默认值）
     * 如果主键为自增，则实体对象会在插入后会被设置主键的值。
     * @param record 实体记录
     * @return 实际插入的条数
     */
    public int insertSelective(T record);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。
     * @param record 实体对象
     * @return 实际更新的条数
     */
    public int updateById(T record);

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。（如字段为空，则不更新）
     * @param record 实体对象
     * @return 实际更新的条数
     */
    public int updateByIdSelective(T record);

}
