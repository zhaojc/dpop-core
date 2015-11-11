package com.baidu.dpop.frame.core.base.it;

import com.baidu.dpop.frame.core.base.BaseService;
import com.baidu.dpop.frame.core.base.GenericMapperDao;

public class UserServiceImpl extends BaseService<User, Long> implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public GenericMapperDao<User, Long> getDao() {
        return userDao;
    }
}
