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
 * @version $Id: TargetSourceCreator.java,v 1.2 2003-12-30 01:07:11 jhoeller Exp $
 */
public interface TargetSourceCreator {
	
	/**
	 * Create a special TargetSource for the given bean, if any.
	 * @param bean the bean to create a TargetSource for
	 * @param beanName the name of the bean
	 * @return a special TargetSource or null if this TargetSourceCreator isn't
	 * interested in the particular bean
	 */
	TargetSource getTargetSource(Object bean, String beanName, BeanFactory factory);

}
