package com.baidu.dpop.frame.core.base;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * BaseDao分页查询单测用例
 */
public class BaseDaoPagingTest {

    /**
     * 总共记录5条，用户查询第1页，每页5条。
     * 那么应该总共只有1页，此时数据库查询0,5。
     */
    @Test
    public void testGetPagedList1() {
        runWithTestCase(5, 1, 5, 1, 0, 5);
    }

    /**
     * 总共记录5条，用户查询第1页，每页4条。
     * 那么应该总共只有2页，此时数据库查询0,4。
     */
    @Test
    public void testGetPagedList2() {
        runWithTestCase(5, 1, 4, 2, 0, 4);
    }

    /**
     * 总共记录5条，用户查询第2页，每页4条。
     * 那么应该总共只有2页，此时数据库查询4,4。
     */
    @Test
    public void testGetPagedList3() {
        runWithTestCase(5, 2, 4, 2, 4, 4);
    }

    /**
     * 总共记录1条，用户查询第1页，每页5条。
     * 那么应该总共只有1页，此时数据库查询0,5。
     */
    @Test
    public void testGetPagedList4() {
        runWithTestCase(1, 1, 5, 1, 0, 5);
    }

    /**
     * 总共记录5条，用户查询第2页，每页5条。
     * 那么应该总共只有1页，当前实际页码为1，此时数据库查询0,5。
     */
    @Test
    public void testGetPagedList5() {
        runWithTestCase(5, 2, 5, 1, 1, 0, 5);
    }

    /**
     * 总共记录0条，用户查询第1页，每页5条。
     * 那么应该总共只有1页，当前实际页码为1，数据库不作查询。
     */
    @Test
    public void testGetPagedList6() {
        runWithTestCaseSkipDB(0, 1, 5, 1, 1);
    }

    /**
     * 总共记录0条，用户查询第2页，每页5条。
     * 那么应该总共只有1页，当前实际页码为1，数据库不作查询。
     */
    @Test
    public void testGetPagedList7() {
        runWithTestCaseSkipDB(0, 2, 5, 1, 1);
    }

    private void runWithTestCase(final long count, final long currPage,
                                 final long pageSize,
                                 final long expectedMaxPage,
                                 final long expectedOffset,
                                 final long expectedLimit) {
        runWithTestCase(count, currPage, pageSize, expectedMaxPage, currPage,
                expectedOffset, expectedLimit);
    }

    private void runWithTestCaseSkipDB(final long count, final long currPage,
                                       final long pageSize,
                                       final long expectedMaxPage,
                                       final long expectedCurrPage) {
        final GenericMapper mapper = mock(GenericMapper.class);
        BaseDao<Object, Long> tested = new BaseDao<Object, Long>() {
            @Override
            public GenericMapper<Object, Long> getMapper() {
                return mapper;
            }
        };
        Pageable callback = mock(Pageable.class);
        when(callback.count()).thenReturn(count);
        when(callback.findByRange(anyLong(), anyLong()))
                .thenReturn(Arrays.asList(new Object(), new Object(),
                        new Object(), new Object(), new Object()));

        PagedList<?> pagedList = tested.getPagedList(currPage, pageSize,
                callback);
        Assert.assertNotNull(pagedList);
        Assert.assertEquals(pageSize, pagedList.getPageSize());
        Assert.assertEquals(expectedCurrPage, pagedList.getCurrPage());
        Assert.assertEquals(expectedMaxPage, pagedList.getMaxPage());

        verify(callback, never()).findByRange(anyLong(), anyLong());
    }

    private void runWithTestCase(final long count, final long currPage,
                                 final long pageSize,
                                 final long expectedMaxPage,
                                 final long expectedCurrPage,
                                 final long expectedOffset,
                                 final long expectedLimit) {
        final GenericMapper mapper = mock(GenericMapper.class);
        BaseDao<Object, Long> tested = new BaseDao<Object, Long>() {
            @Override
            public GenericMapper<Object, Long> getMapper() {
                return mapper;
            }
        };
        Pageable callback = mock(Pageable.class);
        when(callback.count()).thenReturn(count);
        when(callback.findByRange(anyLong(), anyLong()))
                .thenReturn(Arrays.asList(new Object(), new Object(),
                        new Object(), new Object(), new Object()));

        PagedList<?> pagedList = tested.getPagedList(currPage, pageSize,
                callback);
        Assert.assertNotNull(pagedList);
        Assert.assertEquals(pageSize, pagedList.getPageSize());
        Assert.assertEquals(expectedCurrPage, pagedList.getCurrPage());
        Assert.assertEquals(expectedMaxPage, pagedList.getMaxPage());

        verify(callback).findByRange(eq(expectedOffset), eq(expectedLimit));
    }
}
