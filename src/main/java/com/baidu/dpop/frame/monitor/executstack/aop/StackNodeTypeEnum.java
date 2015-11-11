package com.baidu.dpop.frame.monitor.executstack.aop;

/**
 * 
 * 监控栈中的实体类型
 * 目前支持java方法、SQL、事务
 * 未来扩展：缓存、JS 等
 * 
 * @author huhailiang <br/>
 * @date: 2014-11-28 20:28:56 <br/>
 * 
 */
public enum StackNodeTypeEnum {

    JS(Byte.valueOf("0"), "Js Method"), 
    METHOD(Byte.valueOf("1"), "JAVA Method"), 
    CACHE(Byte.valueOf("2"), "Cache"), 
    SQL(Byte.valueOf("3"), "JDBC Sql"), 
    TRANSCTION(Byte.valueOf("4"), "JDBC Transaction");

    private Byte id;
    private String desc;

    private StackNodeTypeEnum(Byte id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public Byte getId() {
        return id;
    }

    public void setId(Byte id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
