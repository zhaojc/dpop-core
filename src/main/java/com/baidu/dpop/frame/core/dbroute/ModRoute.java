package com.baidu.dpop.frame.core.dbroute;

import org.apache.commons.lang.StringUtils;

import com.baidu.dpop.frame.core.constant.SymbolConstant;

/**
 * 
 * @ClassName: ModRoute <br>
 * @Description: 取模路由分库分表<br>
 *               配置DEMO:<br>
 *               配置：user_info.id%16={0~7:user1;8~15:user2}#_%04d<br>
 *               表示user_info表对16取模，得到结果如果是0到7在库user1上，8到15在库user2上<br>
 *               如果分两个库数据还是不够抗不住不够分散的话，可以配置成：<br>
 *               ：user_info.id%16={0~3:user1;3~7:user2;7~10:user3;10~15:user4}#_%04d<br>
 *               表名（user_info）可以不配置如：id%16={0~7:user1;8~15:user2}#_%04d
 * @author huhailiang <br>
 * @date 2013-10-27 下午8:34:53 <br>
 * 
 */
public class ModRoute extends AbstractDBRoute {

    private long routeId;

    private int modNum;

    public ModRoute() {

    }

    /**
     * 分库分表： user_info.id%16={0~7:user1;8~15:user2}#_%04d 不分库分表：user_info.id%16={0~15:rmp}#_%04d
     * 
     * @param routeString
     * @throws Exception
     */
    @Override
    public void parseRouteConfig(String routeConfig) throws Exception {
        if (StringUtils.isNotBlank(routeConfig)) {

            String[] routeConfigArr = routeConfig.split(SymbolConstant.SYMBOL_EQUAL);

            if (routeConfigArr.length >= 2) {
                String[] modaArr = routeConfigArr[0].split(SymbolConstant.SYMBOL_MOD);
                if (modaArr.length == 2) {
                    String[] tableInfoArr = modaArr[0].split("\\" + SymbolConstant.SYMBOL_PERIOD);
                    if (tableInfoArr.length == 2) {
                        this.tableNamePrefix = tableInfoArr[0];
                        this.tableRouteFieldName = tableInfoArr[1];
                    } else if (tableInfoArr.length == 1) {
                        this.tableRouteFieldName = tableInfoArr[0];
                    } else {
                        throw new Exception("invalid routeConfig:" + routeConfig);
                    }
                    modNum = Integer.valueOf(modaArr[1]);

                    String[] routeRes = routeConfigArr[1].split(SymbolConstant.SYMBOL_POUND);

                    String routeRes0 =
                            routeRes[0].replace(SymbolConstant.SYMBOL_OPEN_BRACE, "").replace(
                                    SymbolConstant.SYMBOL_CLOSE_BRAE, "");

                    String[] routeItems = routeRes0.split(SymbolConstant.SYMBOL_SEM);

                    for (int i = 0; i < routeItems.length; i++) {
                        String[] routeb = routeItems[i].split(SymbolConstant.SYMBOL_COLON);
                        if (routeb.length == 2) {
                            this.routeRuleMap.put(routeb[0], routeb[1]);
                        } else {
                            throw new Exception("invalid routeConfig" + routeConfig);
                        }
                    }

                    if (routeRes.length >= 2) {
                        tableNamePostfixFormat = routeRes[1];
                    }
                    this.routeConfig = routeConfig;
                } else {
                    throw new Exception("invalid routeConfig:" + routeConfig);
                }
            } else {
                throw new Exception("invalid routeConfig:" + routeConfig);
            }
        }
    }

    public String getDBGroupName() {
        long mod = getMod(routeId);
        for (String key : this.routeRuleMap.keySet()) {
            String[] idrange = key.split(SymbolConstant.SYMBOL_TILDE);
            long low = Long.valueOf(idrange[0]);
            long max = Long.valueOf(idrange[1]);
            if (mod <= max && mod >= low) {
                return routeRuleMap.get(key);
            }
        }
        return null;
    }

    private long getMod(long id) {
        return id % modNum;
    }

    public String getTableName() {
        if (null == tableNamePostfixFormat) {
            return this.getTableNamePrefix();
        }

        return getTableNamePrefix() + getTableNamePostfix();
    }

    public String getTableNamePostfix() {
        if (null == tableNamePostfixFormat) {
            return "";
        }
        long mod = getMod(routeId);
        return String.format(this.tableNamePostfixFormat, mod);
    }

    public String toString() {
        return "db:[" + getDBGroupName() + "] table:[" + getTableName() + "] routeConfig:[" + routeConfig
                + "]routeRuleMap:" + routeRuleMap.toString();
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

}
