package com.baidu.dpop.frame.core.base;

import java.util.Collections;
import java.util.List;

/**
 * 分页查询时返回的分页列表类，包含当前页码、最大页码、页内容量以及当前页中的数据
 *
 * @param <E> 页内数据的类型
 */
public class PagedList<E> {

    /**
     * 第一页的页码为1
     */
    public static final long FIRST_PAGE = 1;

    private long currPage;
    private long maxPage;
    private long pageSize;
    private List<E> dataList;

    /**
     * 构造一个分页列表对象。若dataList为null，则将会被认为是一个emptyList
     *
     * @param currPage 当前页码
     * @param maxPage  最大页码
     * @param pageSize 页内容量
     * @param dataList 当前页中数据
     */
    public PagedList(long currPage, long maxPage, long pageSize,
                     List<E> dataList) throws IllegalArgumentException {
        if (currPage < FIRST_PAGE) {
            throw new IllegalArgumentException(String.format(
                    "currPage must not less than %d", FIRST_PAGE));
        }
        if (currPage > maxPage) {
            throw new IllegalArgumentException(
                    "currPage must not greater than maxPage");
        }

        this.currPage = currPage;
        this.maxPage = maxPage;
        this.pageSize = pageSize;
        this.dataList = (dataList == null)
                ? Collections.<E>emptyList()
                : Collections.unmodifiableList(dataList);
    }

    /**
     * 构造一个空的分页，以表示当前的分页查询没有任何返回结果
     *
     * @param pageSize 页内容量
     */
    public PagedList(long pageSize) {
        this(FIRST_PAGE, FIRST_PAGE, pageSize, Collections.<E>emptyList());
    }

    /**
     * 获取当前页码（从1开始）
     *
     * @return 当前页码（从1开始）
     */
    public long getCurrPage() {
        return currPage;
    }

    /**
     * 获取最大页码
     *
     * @return 最大页码
     */
    public long getMaxPage() {
        return maxPage;
    }

    /**
     * 获取页内容量
     *
     * @return 页内容量
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 获取当前页中的数据，若没有数据，则返回一个emptyList。
     *
     * @return 当前页中的数据。返回的列表是不可修改的。
     */
    public List<E> getDataList() {
        return dataList;
    }
}
