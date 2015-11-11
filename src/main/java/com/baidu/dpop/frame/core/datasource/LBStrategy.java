
package com.baidu.dpop.frame.core.datasource;

/**   
* 
* DataSource选择的均衡策略接口
* 
* @author cgd  
* @date 2014年8月22日 下午3:51:15 
*/
public interface LBStrategy<T> {

	/**
	 * 根据策略选择一个DataSource
	 * */
	public T elect();
	
	/**
	 * 移除失效的Target
	 * */
	public void removeTarget(T t);
	
	/**
	 * 加入重新生效的Target
	 * */
	public void recoverTarget(T t);
}
