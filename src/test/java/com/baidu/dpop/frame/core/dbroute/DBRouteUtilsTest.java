package com.baidu.dpop.frame.core.dbroute;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * 
 * @author huhailiang
 * @date 2014-10-23 下午8:07:30
 * 
 */
public class DBRouteUtilsTest {

    DBRouteUtils dBRouteUtils = new DBRouteUtils();

    @Before
    public void setUp() {
        Map<String, String> dbRoutePropertiesMap = new HashMap<String, String>();
        dbRoutePropertiesMap.put("db.shard.user",
                "com.baidu.dpop.frame.core.dbroute.ModRoute[id%4={0~1:shard_01;2~3:shard_02}#_%04d]");
        dbRoutePropertiesMap.put("db.shard.test",
                "com.baidu.dpop.frame.core.dbroute.ModRoute[test_info.id%8={0~7:rmp}#_%03d]");
        dbRoutePropertiesMap.put("com.baidu.dpop.frame.core.dbroute.DBRouteUtilsTest",
                "com.baidu.dpop.frame.core.dbroute.ModRoute[tb_ad_info.id%128={0~127:amp}#_%03d]");
        dBRouteUtils.setDbRoutePropertiesMap(dbRoutePropertiesMap);
        dBRouteUtils.init();
    }

    @Test
    public void testgGetUserRoute() {
        ModRoute userModRoute = dBRouteUtils.getRoute("db.shard.user");
        Assert.notNull(userModRoute);
        userModRoute.setRouteId(102);
        Assert.isTrue(userModRoute.getTableName().equals("_0002"));
        Assert.isTrue(userModRoute.getDBGroupName().equals("shard_02"));
    }

    @Test
    public void testgGetTestRoute() {
        ModRoute testModRoute = dBRouteUtils.getRoute("db.shard.test");
        Assert.notNull(testModRoute);
        testModRoute.setRouteId(102);
        Assert.isTrue(testModRoute.getTableName().equals("test_info_006"));
        Assert.isTrue(testModRoute.getDBGroupName().equals("rmp"));
    }

    @Test
    public void testgGetTestRouteByClass() {
        ModRoute testDaoModRoute = dBRouteUtils.getRoute(DBRouteUtilsTest.class.getName());
        Assert.notNull(testDaoModRoute);
        testDaoModRoute.setRouteId(1020202);
        Assert.isTrue(testDaoModRoute.getTableName().equals("tb_ad_info_042"));
        Assert.isTrue(testDaoModRoute.getDBGroupName().equals("amp"));
    }

}
