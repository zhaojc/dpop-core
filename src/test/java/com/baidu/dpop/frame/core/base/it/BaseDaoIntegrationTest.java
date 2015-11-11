package com.baidu.dpop.frame.core.base.it;

import junit.framework.Assert;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class BaseDaoIntegrationTest {

    private UserService userService;

    @Before
    public void setUp() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession session = sqlSessionFactory.openSession();
        UserMapper userMapper = session.getMapper(UserMapper.class);
        UserDao userDao = new UserDaoImpl(userMapper);
        this.userService = new UserServiceImpl(userDao);
    }

    @Test
    public void testFind1() {
        User user = userService.findById(1L);
        Assert.assertNull(user);
    }

    @Test
    public void testInsert() {
        User user = new User();
        user.setId(1L);
        user.setName("user1");
        int row =  userService.insert(user);
        Assert.assertEquals(1, row);
    }

    @Test
    public void testInsertAndFind() {
        User newRecord = new User();
        newRecord.setId(1L);
        newRecord.setName("user1");
        userService.insert(newRecord);

        User user = userService.findById(1L);
        Assert.assertNotNull(user);
        Assert.assertEquals(Long.valueOf(1L), user.getId());
        Assert.assertEquals("user1", user.getName());
    }

    @Test
    public void testInsertUpdateAndFind() {
        User record = new User();
        record.setId(1L);
        record.setName("user1");
        userService.insert(record);

        record.setName("user2");
        userService.updateById(record);

        User user = userService.findById(1L);
        Assert.assertNotNull(user);
        Assert.assertEquals(Long.valueOf(1L), user.getId());
        Assert.assertEquals("user2", user.getName());
    }

    @Test
    public void testInsertDeleteAndFind() {
        User record = new User();
        record.setId(1L);
        record.setName("user1");
        userService.insert(record);

        userService.deleteById(1L);

        User user = userService.findById(1L);
        Assert.assertNull(user);
    }
}
