/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.dpop.frame.monitor.jdbc.dialect;

/**
 * @author huhailiang
 * @date 2015年1月9日
 */
public enum DriverEnum {

    MYSQL("com.mysql.jdbc.Driver", "MYSQL"), 
    Oracle1("oracle.jdbc.OracleDriver", "Oracle1"), 
    Oracle2("oracle.jdbc.driver.OracleDriver", "Oracle2"), 
    H2("org.h2.Driver", "H2"), 
    SQLServer("com.microsoft.jdbc.sqlserver.SQLServerDriver", "SQLServer");

    private String driverClassName;
    private String driverDesc;

    private DriverEnum(String driverClassName, String driverDesc) {
        this.driverClassName = driverClassName;
        this.driverDesc = driverDesc;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDriverDesc() {
        return driverDesc;
    }

    public void setDriverDesc(String driverDesc) {
        this.driverDesc = driverDesc;
    }

}
