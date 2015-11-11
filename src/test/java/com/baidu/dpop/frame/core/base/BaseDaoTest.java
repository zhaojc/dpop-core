package com.baidu.dpop.frame.core.base;

import static org.mockito.Mockito.*;

import org.junit.Test;

public class BaseDaoTest {

    @Test
    public void testDeleteByPrimaryKey() throws Exception {
        GenericMapper mapper = mock(GenericMapper.class);
        BaseDao tested = constructBaseDao(mapper);
        Integer id = 1;

        tested.deleteByPrimaryKey(id);

        verify(mapper).deleteByPrimaryKey(same(id));
    }

    @Test
    public void testInsert() throws Exception {
        GenericMapper mapper = mock(GenericMapper.class);
        BaseDao tested = constructBaseDao(mapper);
        Object record = new Object();

        tested.insert(record);

        verify(mapper).insert(same(record));
    }

    @Test
    public void testInsertSelective() throws Exception {
        GenericMapper mapper = mock(GenericMapper.class);
        BaseDao tested = constructBaseDao(mapper);
        Object record = new Object();

        tested.insertSelective(record);

        verify(mapper).insertSelective(same(record));
    }

    @Test
    public void testSelectByPrimaryKey() throws Exception {
        GenericMapper mapper = mock(GenericMapper.class);
        BaseDao tested = constructBaseDao(mapper);
        Integer id = 1;

        tested.selectByPrimaryKey(id);

        verify(mapper).selectByPrimaryKey(same(id));
    }

    @Test
    public void testUpdateByPrimaryKeySelective() throws Exception {
        GenericMapper mapper = mock(GenericMapper.class);
        BaseDao tested = constructBaseDao(mapper);
        Object record = new Object();

        tested.updateByPrimaryKeySelective(record);

        verify(mapper).updateByPrimaryKeySelective(same(record));
    }

    @Test
    public void testUpdateByPrimaryKey() throws Exception {
        GenericMapper mapper = mock(GenericMapper.class);
        BaseDao tested = constructBaseDao(mapper);
        Object record = new Object();

        tested.updateByPrimaryKey(record);

        verify(mapper).updateByPrimaryKey(same(record));
    }

    private BaseDao constructBaseDao(final GenericMapper mapper) {
        return new BaseDao() {

            @Override
            public GenericMapper getMapper() {
                return mapper;
            }
        };
    }
}
