package com.baidu.dpop.frame.core.base.web;

import java.io.Serializable;

/**
 * 
 * ClassName: Form <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * date: 2014-7-18 上午9:28:55 <br/>
 *
 * @author huhailiang
 * @version @param <T>
 * @since JDK 1.6
 */
public interface IForm<T, ID extends Serializable> {

	
   public T transformBO();
}
