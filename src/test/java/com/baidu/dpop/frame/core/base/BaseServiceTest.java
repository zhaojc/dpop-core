package com.baidu.dpop.frame.core.base;

import static org.mockito.Mockito.*;

import org.junit.Test;

public class BaseServiceTest {

    @Test
    public void testFindById() throws Exception {
        GenericMapperDao dao = mock(GenericMapperDao.class);
        BaseService tested = constructBaseService(dao);

        Integer id = 1;
        tested.findById(id);

        verify(dao).selectByPrimaryKey(eq(id));
    }

    @Test
    public void testDeleteById() throws Exception {
        GenericMapperDao dao = mock(GenericMapperDao.class);
        BaseService tested = constructBaseService(dao);

        Integer id = 1;
        tested.deleteById(id);

        verify(dao).deleteByPrimaryKey(eq(id));
    }

    @Test
    public void testInsert() throws Exception {
        GenericMapperDao dao = mock(GenericMapperDao.class);
        BaseService tested = constructBaseService(dao);

        Object record = new Object();
        tested.insert(record);

        verify(dao).insert(same(record));
    }

    @Test
    public void testUpdateById() throws Exception {
        GenericMapperDao dao = mock(GenericMapperDao.class);
        BaseService tested = constructBaseService(dao);

        Object record = new Object();
        tested.updateById(record);

        verify(dao).updateByPrimaryKey(record);
    }

    private BaseService constructBaseService(final GenericMapperDao dao) {
        return new BaseService() {
            @Override
            public GenericMapperDao getDao() {
                return dao;
            }
        };
    }
}
