package org.springframework.beans.factory.dynamic;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * TargetSource that can apply to singletons,
 * reloading them on request from a child factory.
 * @author Rod Johnson
 * @version $Id: DynamicBeanTargetSource.java,v 1.1 2004-08-03 12:02:08 johnsonr Exp $
 */
public class DynamicBeanTargetSource extends AbstractRefreshableTargetSource {

	private DefaultListableBeanFactory childFactory;
	
	private String beanName;

	public DynamicBeanTargetSource(BeanFactory factory, String beanName) {
		super(null);
		this.beanName = beanName;
		this.childFactory = new DefaultListableBeanFactory(factory);
		
		//RootBeanDefinition definition = (RootBeanDefinition) definitionRegistry.getBeanDefinition(beanName);
		
		// Create a child bean definition that is a prototype,
		// but otherwise copies from the parent
		ChildBeanDefinition definition = new ChildBeanDefinition(beanName, null);
		
		definition.setSingleton(false);

		childFactory.registerBeanDefinition(beanName, definition);
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.AbstractRefreshableTargetSource#refreshedTarget()
	 */
	protected Object refreshedTarget() {
		return childFactory.getBean(beanName);
	}

}