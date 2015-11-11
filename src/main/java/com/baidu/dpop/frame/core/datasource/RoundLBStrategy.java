
package com.baidu.dpop.frame.core.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**   
* @Title: RoundLBStrategy.java 
* 
* DataSource选择的均衡策略
* 配置为dataSourceA:2, dataSourceB:3， 则targets数组存储为2个dataSourceA，3个dataSourceB；
* 然后随机从里面选择一个作为DataSourceTarget;
* 则40%选择几率为dataSourceA， 60%为dataSourceB；
* 
* @author cgd  
* @date 2014年8月22日 下午2:30:24 
* @version V1.0   
*/
public class RoundLBStrategy implements LBStrategy<String> {
	
	// DataSource Target KEYs
	private List<String> targets = new ArrayList<String>();
	
    // 负载因子默认最小值为1
    private static final int MIN_LB_FACTOR = 1;
    
    
    /**
     * 策略器构造函数
     * */
    public RoundLBStrategy(Map<String, Integer> factors) {
    	// 没有配置则不作处理
    	if(factors == null || factors.size() == 0) {
    		return;
    	}
    	
    	// 相关参数初始化
    	this.init(factors);
    }
    
    /**
     * 策略相关参数初始化
     * */
    private void init(Map<String, Integer> factors) {
    	// 如果配置的因子不符合规范就校正
    	Set<Map.Entry<String, Integer>> setEntries = factors.entrySet();
        for (Map.Entry<String, Integer> entry : setEntries) {
            if (entry.getValue() < MIN_LB_FACTOR) {
                entry.setValue(MIN_LB_FACTOR);
            }
        }
        
        // 按照配置的因子比例初始化DataSouce Target 数组
        for(Map.Entry<String, Integer> entry : setEntries) {
        	String factorKey = entry.getKey();
        	Integer factorValue = entry.getValue();
        	for(int i=0; i< factorValue; ++i) {
        		this.targets.add(factorKey);
        	}
        }
    }
    
    
    /**
     * 随机选择一个DataSource Target
     * */
    public synchronized String elect() {
    	Integer arrSize = this.targets.size();
    	Random random = new Random();
    	
    	return this.targets.get(random.nextInt(arrSize));
    }

	@Override
	public void removeTarget(String target) {
		List<String> newTargetList = new ArrayList<String>();
		if(targets != null && targets.size() > 0) {
			for(String item : targets) {
				if(!item.equals(target)) {
					newTargetList.add(item);
				}
			}
		}
		
		targets = newTargetList;
	}

	@Override
	public void recoverTarget(String target) {
		
	}
	
}
