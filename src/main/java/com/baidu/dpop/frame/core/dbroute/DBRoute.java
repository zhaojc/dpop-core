package com.baidu.dpop.frame.core.dbroute;

/**
 * 
 * @ClassName: DBRoute
 * @Description: 分库分表路由接口，此接口负责对外暴露 DBGroupName和tableName<br>
 * @author huhailiang
 * @date 2014-10-23 11:03:04
 * 
 */
public interface DBRoute {

    /**
     * 
     * @Title: getDBName
     * @Description: 获取数据库集群名称，如果不是集群此处是库名
     * @return String
     * @throws
     */
    String getDBGroupName();

    /**
     * 
     * @Title: getTableName
     * @Description: 获取表名称
     * @return String
     * @throws
     */
    String getTableName();
}
