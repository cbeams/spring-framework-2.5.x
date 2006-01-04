package org.springframework.beans.factory.dynamic;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.aop.target.dynamic.AbstractRefreshableTargetSource;

/**
 * TargetSource that can apply to singletons,
 * reloading them on request from a child factory.
 * @author Rod Johnson
 */
public class DynamicBeanTargetSource extends AbstractRefreshableTargetSource {

	private String beanName;

	private DefaultListableBeanFactory childFactory;

	/**
	 * 
	 * @param initialTarget
	 * @param factory
	 * @param beanName
	 * @param childFactory optional, must be a child of factory.
	 * Allows shared child factory.
	 */
	public DynamicBeanTargetSource(BeanFactory factory, String beanName, DefaultListableBeanFactory childFactory) {
		//super(initialTarget);
		this.beanName = beanName;
		this.childFactory = (childFactory == null) ?
			new DefaultListableBeanFactory(factory) :
			childFactory;

		// The child bean definition is a prototype, so whenever
		// we call getBean() on it we'll get a fresh object,
		// configured the same way.
		// Apart from that, the child bean definition will be
		// the same as the parent: all properties are inherited
		ChildBeanDefinition definition = new ChildBeanDefinition(beanName, null);
		definition.setSingleton(false);
		this.childFactory.registerBeanDefinition(beanName, definition);
	}


	/**
	 * @see org.springframework.beans.factory.dynamic.AbstractRefreshableTargetSource#refreshedTarget()
	 */
	protected Object freshTarget() {
		Object o = childFactory.getBean(beanName);
		return o;
	}

	public boolean isModified() {
		return true;
	}
}