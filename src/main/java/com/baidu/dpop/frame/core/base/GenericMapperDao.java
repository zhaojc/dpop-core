package com.baidu.dpop.frame.core.base;

import java.io.Serializable;

/**
 * 数据访问接口，包含基本的增删改查操作以及分页查询接口。
 *
 * 创建日期：2014-6-26
 *
 * @author : huhailiang
 */
public interface GenericMapperDao<T, ID extends Serializable> {

    /**
     * 分页查询，通过传入当前页码以及每页的容量来查询数据库中的相关数据。
     * 如果当前页数大于最大页数（非法页码访问），则返回最后一页。
     *
     * 在以下情况下，传入的参数非法：
     * （1）当前页码小于{@link com.baidu.dpop.frame.core.base.PagedList#FIRST_PAGE}
     * （2）每页容量小于1
     * （3）callback为null
     *
     * @param currPage 当前页码，第一页从{@link com.baidu.dpop.frame.core.base.PagedList#FIRST_PAGE}开始
     * @param pageSize 每页的容量
     * @param callback 分页回调组建，用于指定本方法所需的（1）数据记录条数查询；（2）数据库范围查询。
     * @return 对应页码、页容量的分页列表，其中包含了页面的数据与基本分页信息。当查询没有相关数据时，返回的对象也不为null。
     * @throws java.lang.IllegalArgumentException 当传入参数非法时，详见上述方法描述。
     *
     * @see com.baidu.dpop.frame.core.base.PagedList
     * @see com.baidu.dpop.frame.core.base.Pageable
     */
    PagedList<T> getPagedList(long currPage, long pageSize, Pageable<T> callback);

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
