package com.baidu.dpop.frame.core.base;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import java.io.Serializable;
import java.util.List;


/**
 * 数据访问类（DAO）基类，{@link GenericMapperDao}的抽象实现类，
 * 已实现对于数据库最基本的增删改查操作。实现子类需要实现{@link #getMapper()}接口，
 * 返回对应的数据映射对象。
 *
 * 创建日期：2014-6-26 5:25:24
 *
 * @param <T> 业务实体类
 * @param <ID> 实体的主键类型
 * @author huhailiang
 */
public abstract class BaseDao<T, ID extends Serializable>
        extends SqlSessionDaoSupport implements GenericMapperDao<T, ID> {

    /**
     * 获取数据映射对象
     * @see com.baidu.dpop.frame.core.base.GenericMapper
     * @return 数据映射对象
     */
    public abstract GenericMapper<T, ID> getMapper();

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
    public PagedList<T> getPagedList(long currPage, long pageSize,
                                     Pageable<T> callback) {
        if (currPage < PagedList.FIRST_PAGE) {
            throw new IllegalArgumentException(String.format(
                    "currPage must not less than %d", PagedList.FIRST_PAGE));
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be at least 1");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback is required");
        }

        long count = callback.count();
        if (count <= 0) {
            return new PagedList<T>(pageSize);
        }

        long maxPage = count / pageSize + (count % pageSize == 0 ? 0 : 1) +
                PagedList.FIRST_PAGE - 1;
        if (currPage > maxPage) {
            // 如果当前页数大于最大页数（非法页码访问），则返回最后一页
            currPage = maxPage;
        }
        long offset = (currPage - PagedList.FIRST_PAGE) * pageSize;
        List<T> dataList = callback.findByRange(offset, pageSize);
        return new PagedList<T>(currPage, maxPage, pageSize, dataList);
    }

    /**
     * 根据主键删除数据库中的数据
     * @param id 主键值
     * @return 实际删除的条数
     */
    public int deleteByPrimaryKey(ID id) {
        return getMapper().deleteByPrimaryKey(id);
    }

    /**
     * 插入一条新的实体记录
     * @param record 实体记录
     * @return 实际插入的条数
     */
    public int insert(T record) {
        return getMapper().insert(record);
    }

    /**
     * 插入一条新的实体记录，如果实体对象中的属性为null，则这个属性对应的字段值将不指定（若数据库字段有默认值，则设置为默认值）
     * 如果主键为自增，则实体对象会在插入后会被设置主键的值。
     * @param record 实体记录
     * @return 实际插入的条数
     */
    public int insertSelective(T record) {
        return getMapper().insertSelective(record);
    }

    /**
     * 根据主键查询数据库中的记录。
     * @param id 主键值
     * @return 主键对应的实体记录。若不存在，则返回null。
     */
    public T selectByPrimaryKey(ID id) {
        return getMapper().selectByPrimaryKey(id);
    }

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。若实体对象的属性值为null，则对应数据字段值就不会做更新。
     * @param record 实体对象
     * @return 实际更新的条数
     */
    public int updateByPrimaryKeySelective(T record) {
        return getMapper().updateByPrimaryKeySelective(record);
    }

    /**
     * 根据传入实体对象的主键，更新对应记录的各字段值。
     * @param record 实体对象
     * @return 实际更新的条数
     */
    public int updateByPrimaryKey(T record) {
        return getMapper().updateByPrimaryKey(record);
    }
}
