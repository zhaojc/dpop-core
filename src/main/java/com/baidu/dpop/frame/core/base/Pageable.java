package com.baidu.dpop.frame.core.base;

import java.util.List;

/**
 * 分页回调接口，用于适配不同数据库的查询接口。
 *
 * @param <T> 业务实体对象类型
 */
public interface Pageable<T> {

    /**
     * 返回所有记录的条数
     * @return 所有记录的条数
     */
    long count();

    /**
     * 根据范围查询数据库中的记录
     * @param offset 起始下标（从0开始）
     * @param limit 最多查询条数
     * @return 从起始下标offset开始包含最多limit条数据的列表
     */
    List<T> findByRange(long offset, long limit);
}
