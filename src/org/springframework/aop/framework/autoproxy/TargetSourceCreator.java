/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Implementations can create special target sources, such as pooling target
 * sources, for particular beans. For example, they may base their choice
 * on attributes, such as a pooling attribute, on the target class.
 * AbstractAutoProxyCreator can support a number of TargetSourceCreators,
 * which will be applied in order.
 * @author Rod Johnson
 * @version $Id: TargetSourceCreator.java,v 1.1 2003-12-12 16:50:43 johnsonr Exp $
 */
public interface TargetSourceCreator {
	
	/**
	 * 
	 * @param bean
	 * @param beanName
	 * @return a special TargetSource or null if this TargetSourceCreator isn't
	 * interested in the particular bean
	 */
	TargetSource getTargetSource(Object bean, String beanName, BeanFactory factory);

}
