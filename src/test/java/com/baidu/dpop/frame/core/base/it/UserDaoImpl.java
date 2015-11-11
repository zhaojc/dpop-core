package com.baidu.dpop.frame.core.base.it;

import com.baidu.dpop.frame.core.base.BaseDao;
import com.baidu.dpop.frame.core.base.GenericMapper;

public class UserDaoImpl extends BaseDao<User, Long> implements UserDao {

    private final UserMapper userMapper;

    public UserDaoImpl(UserMapper mapper) {
        this.userMapper = mapper;
    }

    @Override
    public GenericMapper<User, Long> getMapper() {
        return userMapper;
    }
}
