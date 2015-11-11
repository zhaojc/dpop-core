package com.baidu.dpop.frame.core.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author cgd
 * @version V1.0
 * @Title: DynamicDataSource.java
 * @Package com.baidu.dpop.frame.core.datasource
 * @Description: 动态数据源管理组件，用于挂接类似DBproxy，支持负载
 * @date 2014年8月22日 下午3:44:51
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private final Logger logger = Logger.getLogger(this.getClass());

    // DataSource选择策略接口
    private LBStrategy<String> strategy;

    // 保存当前线程的DataSource KEY
    private static final ThreadLocal<String> SELECTED_KEY = new ThreadLocal<String>();

    /**
     * 获取DB连接
     *
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        return this.getConnectionFromDataSource(null, null);
    }

    /**
     * 通过用户名、密码获取DB连接
     *
     * @throws SQLException
     */
    @Override
    public Connection getConnection(String userName, String password) throws SQLException {
        return this.getConnectionFromDataSource(userName, password);
    }

    /**
     * 获取当前DataSource Name
     */
    @Override
    public Object determineCurrentLookupKey() {
        return SELECTED_KEY.get();
    }

    // ----------------------------------------------------------

    /**
     * 通过配置的DataSource获取DB连接
     *
     * @throws SQLException
     */
    private Connection getConnectionFromDataSource(String userName, String password) throws SQLException {
        // 获取DataSource Name
        String dataSourceName = this.electDataSourceName();
        if (dataSourceName == null) {
            throw new SQLException("No More DataSource Is Available.");
        }

        Connection con = null;
        try {
            DataSource dataSource = determineTargetDataSource();
            if (userName != null && password != null) {
                con = dataSource.getConnection(userName, password);
            } else {
                con = dataSource.getConnection();
            }
        } catch (Exception e) {
            logger.error("Failed to getConnection() from the data source. It will be removed from the pool.", e);
            // 若选中的连接失败则重新选择配置的其他连接
            this.strategy.removeTarget(dataSourceName);

            // 重新获取连接
            return getConnectionFromDataSource(userName, password);

        } finally {
            // 资源清理
            SELECTED_KEY.remove();
        }

        return con;
    }

    /**
     * 根据均衡策略选择合适的DataSource
     */
    private String electDataSourceName() {
        String sourceName = this.strategy.elect();

        // 存储到threadLocal中
        SELECTED_KEY.set(sourceName);

        return sourceName;
    }

    // ---------------------------------------------------
    public void setStrategy(LBStrategy<String> strategy) {
        this.strategy = strategy;
    }

}
