package com.baidu.dpop.frame.core.base.dbroute;

import java.io.Serializable;

import com.baidu.dpop.frame.core.dbroute.DBRoute;

/**
 * 分表数据访问类（DAO）基类（主键ID用ID生成器生成）
 * 
 * @author cgd
 * @date 2015年7月24日 下午2:29:33
 */
public abstract class UniqueIdRouteBaseDao<T, ID extends Serializable, RID extends Serializable> extends
        RouteBaseDao<T, ID, RID> {

    /**
     * 用ID生成器生成主键ID并set到BO
     * */
    public abstract void setPrimaryId(T record);

    /**
     * 插入一条新的实体记录
     * 
     * @param record 实体记录
     * @return 实际插入的条数
     */
    @Override
    public int insert(T record, RID routerId) {
        // 生成主键ID
        this.setPrimaryId(record);
        DBRoute route = getDBRoute(routerId);
        return getMapper().insert(record, route);
    }

    /**
     * 插入一条新的实体记录，如果实体对象中的属性为null，则这个属性对应的字段值将不指定（若数据库字段有默认值，则设置为默认值）<br>
     * 如果主键为自增，则实体对象会在插入后会被设置主键的值。<br>
     * 
     * @param record 实体记录
     * @return 实际插入的条数
     */
    @Override
    public int insertSelective(T record, RID routerId) {
        // 生成主键ID
        this.setPrimaryId(record);
        DBRoute route = getDBRoute(routerId);
        return getMapper().insertSelective(record, route);
    }

}
