package org.springframework.beans.factory.dynamic;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * TargetSource that can apply to singletons,
 * reloading them on request from a child factory.
 * @author Rod Johnson
 * @version $Id: DynamicBeanTargetSource.java,v 1.2 2004-08-04 16:49:47 johnsonr Exp $
 */
public class DynamicBeanTargetSource extends AbstractRefreshableTargetSource {

	private DefaultListableBeanFactory childFactory;
	
	private String beanName;

	public DynamicBeanTargetSource(Object initialTarget, BeanFactory factory, String beanName) {
		super(initialTarget);
		this.beanName = beanName;
		this.childFactory = new DefaultListableBeanFactory(factory);
		
		ChildBeanDefinition definition = new ChildBeanDefinition(beanName, null);
		
		definition.setSingleton(false);

		childFactory.registerBeanDefinition(beanName, definition);	
	}
	

	/**
	 * @see org.springframework.beans.factory.dynamic.AbstractRefreshableTargetSource#refreshedTarget()
	 */
	protected Object refreshedTarget() {
		Object o = childFactory.getBean(beanName);
		return o;
	}
}